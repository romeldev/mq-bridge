package com.dira.mqbridge.service;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import com.ibm.mq.constants.MQConstants;

@Service
public class MqClientService {

    private static final Logger log = LoggerFactory.getLogger(MqClientService.class);

    public MqSendResponse sendAndReceive(MqSendRequest req) {
        MqCredentials creds = req.getCredentials();
        MqOptions opts = req.getOptions() != null ? req.getOptions() : new MqOptions();

        MQQueueManager queueManager = null;
        MQQueue inputMqQueue = null;
        MQQueue outputMqQueue = null;

        try {
            Hashtable<String, Object> env = new Hashtable<>();
            env.put(MQConstants.HOST_NAME_PROPERTY, creds.getHost());
            env.put(MQConstants.PORT_PROPERTY, creds.getPort());
            env.put(MQConstants.CHANNEL_PROPERTY, creds.getChannel());
            env.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);

            if (creds.getUser() != null) {
                env.put(MQConstants.USER_ID_PROPERTY, creds.getUser());
            }
            if (creds.getPassword() != null) {
                env.put(MQConstants.PASSWORD_PROPERTY, creds.getPassword());
            }

            log.info("Connecting to MQ: {}:{} channel={} qm={}",
                    creds.getHost(), creds.getPort(), creds.getChannel(), creds.getQueueManager());

            queueManager = new MQQueueManager(creds.getQueueManager(), env);

            // Send message to input queue
            inputMqQueue = queueManager.accessQueue(req.getInputQueue(), MQConstants.MQOO_OUTPUT);
            MQMessage inputMessage = new MQMessage();
            inputMessage.format = MQConstants.MQFMT_STRING;
            inputMessage.writeString(req.getMessage());
            inputMqQueue.put(inputMessage, new MQPutMessageOptions());
            inputMqQueue.close();
            inputMqQueue = null;

            log.info("Message sent to queue: {}", req.getInputQueue());

            // Read response from output queue with retries
            outputMqQueue = queueManager.accessQueue(req.getOutputQueue(),
                    MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);

            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT;
            gmo.waitInterval = opts.getTimeoutMs();

            int retries = opts.getRetries();
            int retriesUsed = 0;

            for (int i = 0; i <= retries; i++) {
                try {
                    MQMessage outputMessage = new MQMessage();
                    outputMqQueue.get(outputMessage, gmo);
                    String responseData = outputMessage.readStringOfByteLength(outputMessage.getMessageLength());
                    outputMqQueue.close();
                    outputMqQueue = null;

                    log.info("Response received from queue: {} (attempt {})", req.getOutputQueue(), i + 1);
                    return MqSendResponse.ok(responseData, retriesUsed);

                } catch (MQException mqEx) {
                    if (mqEx.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE && i < retries) {
                        retriesUsed++;
                        log.warn("No message available, retrying ({}/{})", retriesUsed, retries);
                        Thread.sleep(opts.getRetryWaitMs());
                    } else {
                        throw mqEx;
                    }
                }
            }

            return MqSendResponse.fail("Max retries exceeded without response");

        } catch (MQException e) {
            String reasonName = MQConstants.lookup(e.reasonCode, "MQRC_.*");
            log.error("MQ error: CC={} RC={} ({}) - {}", e.completionCode, e.reasonCode, reasonName, e.getMessage());
            return MqSendResponse.fail("MQ error (RC=" + e.reasonCode + " " + reasonName + "): " + e.getMessage());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return MqSendResponse.fail(e.getMessage());
        } finally {
            closeQuietly(inputMqQueue);
            closeQuietly(outputMqQueue);
            disconnectQuietly(queueManager);
        }
    }

    private void closeQuietly(MQQueue queue) {
        if (queue != null) {
            try {
                queue.close();
            } catch (MQException e) {
                log.warn("Error closing queue: {}", e.getMessage());
            }
        }
    }

    private void disconnectQuietly(MQQueueManager qm) {
        if (qm != null) {
            try {
                qm.disconnect();
            } catch (MQException e) {
                log.warn("Error disconnecting queue manager: {}", e.getMessage());
            }
        }
    }
}
