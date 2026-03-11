package com.dira.pkmtoolkit.susalud.service;

import java.util.Hashtable;

import org.springframework.stereotype.Service;

import com.ibm.mq.constants.MQConstants;
import com.dira.pkmtoolkit.susalud.request.MqSendRequest;
import com.dira.pkmtoolkit.susalud.response.MqReceiveResponse;
import com.ibm.mq.*;

@Service
public class MqClientService {

    public MqReceiveResponse sendAndReceive(MqSendRequest req) {
        MqReceiveResponse response = new MqReceiveResponse();

        int reintentos = 4;
        int espera = 4000; // en milisegundos

        try {
            // Configurar conexión MQ
            Hashtable<String, Object> env = new Hashtable<>();
            env.put(MQConstants.HOST_NAME_PROPERTY, req.getHost());
            env.put(MQConstants.PORT_PROPERTY, req.getPort().intValue());
            env.put(MQConstants.CHANNEL_PROPERTY, req.getChannel());
            env.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);

            // Crear QueueManager
            MQQueueManager queueManager = new MQQueueManager(req.getQueueManager(), env);

            System.out.println("Channel: " + req.getChannel());
            System.out.println("Host: " + req.getHost());
            System.out.println("Port: " + req.getPort());
            System.out.println("Queue Manager: " + req.getQueueManager());
            System.out.println("Put Queue: " + req.getInputQueue());
            System.out.println("Get Queue: " + req.getOutputQueue());
            System.out.println("Message: " + req.getMessage());

            // Enviar mensaje a inputQueue
            MQQueue inputMqQueue = queueManager.accessQueue(req.getInputQueue(), MQConstants.MQOO_OUTPUT);
            MQMessage inputMessage = new MQMessage();
            inputMessage.format = MQConstants.MQFMT_STRING;
            inputMessage.writeString(req.getMessage());
            inputMqQueue.put(inputMessage, new MQPutMessageOptions());
            inputMqQueue.close();

            // reintentos: 10 + tiempo por intento: 4 segundos

            // Leer respuesta de outputQueue
            MQQueue outputMqQueue = queueManager.accessQueue(req.getOutputQueue(),
                    MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
            MQMessage outputMessage = new MQMessage();
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT;
            gmo.waitInterval = 10000; // 10 segundos de espera (recomendado solo 4)

            // Considerar manejar intentos aqui

            // for (int i = 0; i < reintentos; i++) {
            // try {
            // outputMqQueue.get(outputMessage, gmo);
            // break; // Si se recibe el mensaje, salir del ciclo
            // } catch (MQException mqEx) {
            // if (mqEx.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
            // System.out.println("No message available, retrying... (" + (i + 1) + "/" +
            // reintentos + ")");
            // Thread.sleep(espera);
            // } else {
            // throw mqEx; // Otro error, relanzar la excepción
            // }
            // }
            // }

            outputMqQueue.get(outputMessage, gmo);

            String strMessage = outputMessage.readStringOfByteLength(outputMessage.getMessageLength());
            outputMqQueue.close();

            // System.out.println("Enviando mensaje a MQ: " + req.getMessage());
            // System.out.println("Input Queue: " + req.getInputQueue());
            // System.out.println("Output Queue: " + req.getOutputQueue());

            // Cerrar conexión
            queueManager.disconnect();
            response.setStatus(true);
            response.setData(strMessage);
            response.setReqMessage(req.getMessage());
        } catch (Exception e) {
            System.out.println("Error en comunicación con MQ: " + e.getMessage());
            response.setStatus(false);
            response.setError(e.getMessage());
        }

        return response;
    }
}
