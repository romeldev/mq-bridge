package com.dira.mqbridge.service;

import java.io.IOException;
import java.util.Hashtable;

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
import com.ibm.mq.constants.CMQC;
import org.apache.log4j.Logger;

@Service
public class MqClientService1 {


    public MqSendResponse sendAndReceive(MqSendRequest req) {

        Logger logger = Logger.getLogger("ACREDITACION");
        MQQueueManager queueManager = null;


        MQQueue queueIn = null;
        MQQueue queueOut = null;
        Hashtable<String, Object> props = null;
        MQMessage putMsg = null;
        MQMessage getMsg = null;
        MQGetMessageOptions gmo = null;

        MqCredentials creds = req.getCredentials();
        MqOptions opts = req.getOptions() != null ? req.getOptions() : new MqOptions();


        try {
            // mq properties			
            props = new Hashtable<String, Object>();
            props.put(CMQC.CHANNEL_PROPERTY, creds.getChannel());
            props.put(CMQC.PORT_PROPERTY, creds.getPort());
            props.put(CMQC.HOST_NAME_PROPERTY, creds.getHost());
            props.put(CMQC.APPNAME_PROPERTY, "Pakamuros Soft");
            props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);

            String queueManagername=creds.getQueueManager();
            String queueNameIn = req.getInputQueue();
            String queueNameOut = req.getOutputQueue();
          
            String messageString = req.getMessage();
            // display all details
            /*   
            // create connection
            logger.info("Connecting to queue manager.. ");*/
            queueManager = new MQQueueManager(queueManagername, props);
            //logger.info("done");			

            // accessing queue for both putting message
            //logger.info("Accessing queue " + queueNameIn + ".. ");            
            queueIn = queueManager.accessQueue(queueNameIn, CMQC.MQOO_OUTPUT | CMQC.MQOO_FAIL_IF_QUIESCING);
            //logger.info("done");

            // Send message to input queue
            putMsg = new MQMessage();
            putMsg.characterSet = 819;//MQC.MQCCSI_Q_MGR;
            putMsg.encoding = 273;
            putMsg.format = CMQC.MQFMT_STRING;
            putMsg.writeString(messageString);
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            queueIn.put(putMsg, pmo);
            //logger.info("done");

            // Read response from output queue with retries
            queueOut = queueManager.accessQueue(queueNameOut, CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_FAIL_IF_QUIESCING);
            getMsg = new MQMessage();
            getMsg.characterSet = 819;//MQC.MQCCSI_Q_MGR;
            getMsg.encoding = 273;
            getMsg.format = CMQC.MQFMT_STRING;
            getMsg.messageId = putMsg.messageId;

            // Specify default get message options
            gmo = new MQGetMessageOptions();
            gmo.matchOptions = CMQC.MQMO_MATCH_MSG_ID;
            gmo.options = CMQC.MQGMO_WAIT;
            gmo.waitInterval = 3000;

            logger.info("get");

            int isContinuar = 0;
            int numReIntentos = opts.getRetries();

            System.out.println("waith interval: "+ gmo.waitInterval);
            do {

                try {
                    // Get the message off the queue.					
                    queueOut.get(getMsg, gmo);
                    break;
                } catch (Exception e) {
                    //nothing
                    isContinuar++;
                    if (isContinuar <= numReIntentos) {
                        Thread.sleep(opts.getRetryWaitMs());
                        logger.info("...and getting the message back again: " + isContinuar);
                    }
                }

            } while (isContinuar <= numReIntentos);

            logger.info("done");

            if (getMsg.getDataLength() > 0) {

                // And display the message text...
                logger.info("ready message get");
                String tramaXML = getMsg.readStringOfByteLength(getMsg.getDataLength());
                logger.info("Message got: " + tramaXML);
                System.out.println("Message got: " + tramaXML);
                
                
                //String msgid = new String(Hex.encodeHex(getMsg.messageId));
                //logger.info("Msg Id: "+msgid);
                logger.info("done");

                /*haspMap = new HashMap<>();
				haspMap.put("MsgId", msgid);
				haspMap.put("Msg", tramaXML);*/

                return MqSendResponse.ok(tramaXML, isContinuar);
            }

            return MqSendResponse.fail("No message received after retries");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            logger.error("Error: "+ e.getMessage(), e);
            return MqSendResponse.fail(e.getMessage());
        } finally {

            clearMessage(getMsg, "put");

            clearMessage(putMsg, "put");

            closeQuietly(queueIn, "queueIn");

            closeQuietly(queueOut, "queueOut");

            disconnectQuietly(queueManager);
        }
    }

    private void clearMessage(MQMessage msg, String type)
    {
        if (msg != null) {
            try {
                msg.clearMessage();
            } catch (IOException e) {
                // log.warn("Error closing queue: {}", e.getMessage());
            }
        }
    }

    private void closeQuietly(MQQueue queue, String queueVar) {
        if (queue != null) {
            try {
                queue.close();
            } catch (MQException e) {
                // log.warn("Error closing queue: {}", e.getMessage());
            }
        }
    }

    private void disconnectQuietly(MQQueueManager qm) {
        if (qm != null) {
            try {
                qm.disconnect();
            } catch (MQException e) {
                // logger.warn("Error disconnecting queue manager: "+ e.getMessage());
            }
        }
    }
}
