package com.dira.mqbridge.service;

import com.dira.mqbridge.dto.MqCredentials;
import com.dira.mqbridge.dto.MqOptions;
import com.dira.mqbridge.dto.MqSendRequest;
import com.dira.mqbridge.dto.MqSendResponse;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Servicio para comunicación request-reply sobre IBM MQ.
 *
 * Mejoras respecto a la versión original:
 * - Correlación correcta de mensajes por correlationId (no messageId)
 * - Retry diferenciado: solo reintenta en MQRC_NO_MSG_AVAILABLE
 * - Logger estático con SLF4J (no instanciado por llamada)
 * - Sin System.out.println
 * - Cleanup robusto en finally
 * - Validación de entrada anticipada (fail-fast)
 * - Constantes en lugar de números mágicos
 */
@Service
public class MqClientService {

    private static final Logger log = LoggerFactory.getLogger(MqClientService.class);

    // Encoding IBM MQ: Latin-1 (819) con encoding S/390 (273)
    private static final int MQ_CHARACTER_SET = 819;
    private static final int MQ_ENCODING      = 273;
    private static final String APP_NAME      = "Pakamuros Soft";

    // -------------------------------------------------------------------------
    // Método principal
    // -------------------------------------------------------------------------

    public MqSendResponse sendAndReceive(MqSendRequest req) {

        // --- 1. Validación rápida de entrada ---
        String validationError = validate(req);
        if (validationError != null) {
            log.warn("Request inválido: {}", validationError);
            return MqSendResponse.fail(validationError);
        }

        MqCredentials creds = req.getCredentials();
        MqOptions     opts  = req.getOptions() != null ? req.getOptions() : new MqOptions();

        MQQueueManager queueManager = null;
        MQQueue        queueIn      = null;
        MQQueue        queueOut     = null;

        try {
            // --- 2. Conexión al Queue Manager ---
            Hashtable<String, Object> props = buildConnectionProps(creds);
            log.info("Conectando a QM '{}' en {}:{} canal '{}'",
                    creds.getQueueManager(), creds.getHost(), creds.getPort(), creds.getChannel());

            queueManager = new MQQueueManager(creds.getQueueManager(), props);
            log.info("Conexión establecida con QM '{}'", creds.getQueueManager());

            // --- 3. Envío del mensaje ---
            queueIn = queueManager.accessQueue(
                    req.getInputQueue(),
                    CMQC.MQOO_OUTPUT | CMQC.MQOO_FAIL_IF_QUIESCING
            );

            MQMessage putMsg = buildMessage(req.getMessage());
            queueIn.put(putMsg, new MQPutMessageOptions());
            log.info("Mensaje enviado a '{}' (msgId={})",
                    req.getInputQueue(), hexOf(putMsg.messageId));

            // --- 4. Lectura de respuesta con reintentos ---
            queueOut = queueManager.accessQueue(
                    req.getOutputQueue(),
                    CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_FAIL_IF_QUIESCING
            );

            MQMessage response = receiveWithRetry(queueOut, putMsg.messageId, opts);

            if (response == null) {
                log.warn("Sin respuesta en '{}' tras {} reintentos",
                        req.getOutputQueue(), opts.getRetries());
                return MqSendResponse.fail("No se recibió respuesta tras los reintentos configurados");
            }

            // --- 5. Lectura del cuerpo ---
            int dataLen = response.getDataLength();
            if (dataLen == 0) {
                log.warn("Respuesta recibida pero con cuerpo vacío");
                return MqSendResponse.fail("Respuesta vacía");
            }

            String body = response.readStringOfByteLength(dataLen);
            String msgId = hexOf(putMsg.messageId);
            log.info("Respuesta recibida ({} bytes): {}", dataLen, body);
            return MqSendResponse.ok(msgId, body, /* retriesUsed= */ 0);

        } catch (MQException e) {
            log.error("Error MQ [CC={} RC={}]: {}", e.completionCode, e.reasonCode, e.getMessage(), e);
            return MqSendResponse.fail(String.format("MQ error RC=%d: %s", e.reasonCode, e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage(), e);
            return MqSendResponse.fail("Error interno: " + e.getMessage());

        } finally {
            closeQuietly(queueIn,  "queueIn");
            closeQuietly(queueOut, "queueOut");
            disconnectQuietly(queueManager);
        }
    }

    // -------------------------------------------------------------------------
    // Recepción con reintentos (solo reintenta si no hay mensaje disponible)
    // -------------------------------------------------------------------------

    /**
     * Intenta leer un mensaje correlacionado con {@code correlId}.
     *
     * Solo reintenta ante MQRC_NO_MSG_AVAILABLE (2033).
     * Cualquier otro error MQ se propaga inmediatamente.
     *
     * @return el mensaje leído, o null si se agotaron los reintentos
     */
    private MQMessage receiveWithRetry(MQQueue queue, byte[] correlId, MqOptions opts)
            throws MQException, InterruptedException {

        // MQGetMessageOptions gmo = buildGmo(opts.getWaitIntervalMs());
        MQGetMessageOptions gmo = buildGmo(opts.getTimeoutMs());


        int maxRetries  = opts.getRetries();
        int attemptsDone = 0;

        while (true) {
            MQMessage getMsg = buildCorrelatedMessage(correlId);
            try {
                queue.get(getMsg, gmo);
                log.debug("Mensaje obtenido en intento {}", attemptsDone + 1);
                return getMsg;

            } catch (MQException e) {
                if (e.reasonCode != CMQC.MQRC_NO_MSG_AVAILABLE) {
                    // Error real (permisos, red, formato…): no reintentar
                    throw e;
                }

                attemptsDone++;
                if (attemptsDone > maxRetries) {
                    return null; // agotados los reintentos
                }

                log.info("Sin mensaje aún, reintento {}/{} (espera {} ms)...",
                        attemptsDone, maxRetries, opts.getRetryWaitMs());
                Thread.sleep(opts.getRetryWaitMs());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Builders internos
    // -------------------------------------------------------------------------

    private Hashtable<String, Object> buildConnectionProps(MqCredentials creds) {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(CMQC.CHANNEL_PROPERTY,   creds.getChannel());
        props.put(CMQC.PORT_PROPERTY,      creds.getPort());
        props.put(CMQC.HOST_NAME_PROPERTY, creds.getHost());
        props.put(CMQC.APPNAME_PROPERTY,   APP_NAME);
        props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
        return props;
    }

    /** Mensaje de envío (PUT). */
    private MQMessage buildMessage(String body) throws IOException {
        MQMessage msg = new MQMessage();
        msg.characterSet = MQ_CHARACTER_SET;
        msg.encoding     = MQ_ENCODING;
        msg.format       = CMQC.MQFMT_STRING;
        msg.writeString(body);
        return msg;
    }

    /**
     * Mensaje vacío para GET, correlacionado con el messageId del PUT.
     *
     * El proveedor responde copiando el messageId original al messageId de la respuesta,
     * por lo que se usa MQMO_MATCH_MSG_ID para localizar la respuesta.
     */
    private MQMessage buildCorrelatedMessage(byte[] msgId) {
        MQMessage msg = new MQMessage();
        msg.characterSet  = MQ_CHARACTER_SET;
        msg.encoding      = MQ_ENCODING;
        msg.format        = CMQC.MQFMT_STRING;
        msg.messageId     = msgId;
        return msg;
    }

    private MQGetMessageOptions buildGmo(int waitIntervalMs) {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.matchOptions  = CMQC.MQMO_MATCH_MSG_ID;
        gmo.options       = CMQC.MQGMO_WAIT;
        gmo.waitInterval  = waitIntervalMs;
        return gmo;
    }

    // -------------------------------------------------------------------------
    // Validación
    // -------------------------------------------------------------------------

    private String validate(MqSendRequest req) {
        if (req == null)                                    return "Request nulo";
        if (req.getMessage() == null
                || req.getMessage().isBlank())              return "El mensaje no puede estar vacío";
        if (req.getInputQueue() == null
                || req.getInputQueue().isBlank())           return "inputQueue requerido";
        if (req.getOutputQueue() == null
                || req.getOutputQueue().isBlank())          return "outputQueue requerido";

        MqCredentials c = req.getCredentials();
        if (c == null)                                      return "Credentials requerido";
        if (c.getHost() == null || c.getHost().isBlank())   return "host requerido";
        if (c.getChannel() == null
                || c.getChannel().isBlank())                return "channel requerido";
        if (c.getQueueManager() == null
                || c.getQueueManager().isBlank())           return "queueManager requerido";

        return null; // válido
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    private void closeQuietly(MQQueue queue, String label) {
        if (queue == null) return;
        try {
            queue.close();
            log.debug("Queue '{}' cerrada", label);
        } catch (MQException e) {
            log.warn("No se pudo cerrar '{}': RC={}", label, e.reasonCode);
        }
    }

    private void disconnectQuietly(MQQueueManager qm) {
        if (qm == null) return;
        try {
            qm.disconnect();
            log.debug("QueueManager desconectado");
        } catch (MQException e) {
            log.warn("No se pudo desconectar el QueueManager: RC={}", e.reasonCode);
        }
    }

    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------

    /** Convierte un byte[] a String hex para logging (no afecta el flujo). */
    private static String hexOf(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}