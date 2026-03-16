package com.dira.mqbridge.example;

import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author ccarrillor
 */
public class ExampleInsertLog {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        Logger logger = Logger.getLogger("ACREDITACION");

        MQQueueManager queueManager = null;
        MQQueue queueIn = null;
        MQQueue queueOut = null;
        Hashtable<String, Object> props = null;
        MQMessage putMsg = null;
        MQMessage getMsg = null;
        MQGetMessageOptions getMessageOptions = null;

        try {

            // mq properties			
        
            props = new Hashtable<String, Object>();
            
        

            props.put(CMQC.CHANNEL_PROPERTY, "CH.CLI.EXT3");
            props.put(CMQC.PORT_PROPERTY, 21430);
            props.put(CMQC.HOST_NAME_PROPERTY, "app23.susalud.gob.pe");
            props.put(CMQC.APPNAME_PROPERTY, "Acreditación client JAVA, SUSALUD V0.1");
            props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
            String queueNameOut = "QL.995.AC.002.3.OUT";
            String queueNameIn = "QL.995.AC.002.3.IN";
            String queueManagername="QM.999.998.AC";
          
          
            String messageString2 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<RegistroAutRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.susalud.gob.pe/acreditacion/RegistroAutRequest.xsd\">\n"
                    + "	<txNombre>271_LOGACRE_INSERT</txNombre>\n"
                    + "	<codRemitente>00013591</codRemitente>\n"
                    + "	<txPeticion>ISA*00*          *00*          *ZZ*00013591       *ZZ*10005          *200820*1740*|*00501*000000099*0*T*:~GS*HB*00013591       *10005          *20200826*174056  *032842644*X *00501       ~ST*271*29153320 *                                   ~BHT*0022*13~HL*1           *            *20*1~NM1*PR *2*                                                            *                                   *                         *          *          *PI*20448458836         *  *   *                                                            ~HL*2           *1           *21*1~NM1*1P *2*                                                            *                                   *                         *          *          *FI*10005               *  *   *                                                            ~HL*3           *2           *22*0~NM1*IL *1*TEST APE PAT                                                *TEST NOM                           *                         *          *          *MI*44845883            *  *   *TES APE MAT                                                 ~REF*ACC*6                                                                               *                                                                                ~REF*DD *1                                                                               *                                                                                *4A :44845883            :   :                    :   :                    ~REF*CT *0000000751                                                                      *                                                                                *TY :1                   :   :                    :   :                    ~REF*PRT*02                                                                              *                                                                                ~REF*18 *NUPLAN                                                                          *                                                                                ~REF*ZZ *1                                                                               *                                                                                ~DMG*D8 *19900823                           *1* *                    *  *   ~DTP*356*D8 *20200101                           ~EB*1 *4  *                                   *   *                                                  *                                   *                  *          *  *   *   *                                   ~NM1*P5 *1*                                                            *                                   *                         *          *          *  *                    *  *   *                                                            ~REF*DD *1                                                                               *                                                                                *XX5:20178922581         :   :                    :   :                    ~NM1*C9 *1*                                                            *                                   *                         *          *          *MI*0000424421          *  *   *                                                            ~NM1*IL *2*SALUDPOL                                                    *                                   *                         *          *          *  *                    *  *   *                                                            ~REF*DD *                                                                                *                                                                                *4A :                    :   :                    :   :                    ~REF*ZZ *000000000045                                                                    *                                                                                ~DMG*DT *202008261740                       * * *                    *  *   ~EB*1 *   *                                   *   *                                                  *                                   *0                 *          *  *   *   *                                   ~EB*C *   *                                   *   *                                                  *                                   *0                 *          *  *   *   *                                   ~EB*1 *   *                                   *   *                                                  *                                   *                  *0         *  *   *   *                                   ~EB*0 *   *                                   *   *                                                  *                                   *                  *          *  *   *   *                                   ~SE*30        *29153320 ~GE*1     *032842644~IEA*1    *000000099~</txPeticion>\n"
                    + "</RegistroAutRequest>";


            String messageString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<RegistroAutRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.susalud.gob.pe/acreditacion/RegistroAutRequest.xsd\">\n"
                    + "	<txNombre>271_LOGACRE_INSERT</txNombre>\n"
                    + "	<codRemitente>00004210</codRemitente>\n"
                    + "	<txPeticion>ISA*00*          *00*          *ZZ*00004210       *ZZ*10001          *260316*1101*|*00501*000000120*0*T*:~GS*HB*00004210       *10001          *20260316*110154  *092106278*X *00501       ~ST*271*58568984 *                                   ~BHT*0022*13~HL*1           *            *20*1~NM1*PR *2*                                                            *                                   *                         *          *          *PI*20230089630         *  *   *                                                            ~HL*2           *1           *21*1~NM1*1P *2*                                                            *                                   *                         *          *          *FI*10001               *  *   *                                                            ~HL*3           *2           *22*0~NM1*IL *1*CHAVEZ                                                      *JOSE DAVID                         *                         *          *          *MI*2-71949473          *  *   *RODRIGUEZ                                                   ~REF*ACC*1                                                                               *                                                                                ~REF*DD *1                                                                               *                                                                                *4A :71949473            :   :                    :   :                    ~REF*CT *2-71949473                                                                      *                                                                                *TY :1                   :   :                    :   :                    ~REF*PRT*05                                                                              *                                                                                ~REF*18 *2-71949473                                                                      *                                                                                ~REF*ZZ *1                                                                               *                                                                                ~DMG*D8 *20010412                           *1* *                    *  *   ~DTP*356*D8 *20240814                           ~EB*1 *8   *                                   *   *P05                                               *                                   *                  *          *  *   *   *                                   ~NM1*P5 *1*                                                            *                                   *                         *          *          *  *                    *  *   *                                                            ~REF*DD *1                                                                               *                                                                                *XX5:71949473            :   :                    :   :                    ~NM1*C9 *1*                                                            *                                   *                         *          *          *MI*2-71949473          *  *   *                                                            ~NM1*IL *1*10001                                                       *                                   *                         *          *          *  *                    *  *   *                                                            ~REF*DD *                                                                                *                                                                                *4A :                    :   :                    :   :                    ~REF*ZZ *079681451                                                                       *                                                                                ~DMG*DT *202603161025                       * * *                    *  *   ~EB*1 *    *                                   *   *                                                  *                                   *999999            *          *  *   *   *                                   ~EB*C *    *                                   *   *                                                  *                                   *0.0               *          *  *   *   *                                   ~EB*1 *    *                                   *   *                                                  *                                   *                  *100.00    *  *   *   *                                   ~EB*0 *    *                                   *   *SIS PARA TODOS: PEAS + PLANES COMPLEMENTARIOS     *                                   *                  *          *  *   *   *                                   ~SE*30        *58568984 ~GE*1     *092106278~IEA*1    *000000120~</txPeticion>\n"
                    + "</RegistroAutRequest>";
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

            // creating a message to put
            // the message is related to a afiliacion
            // the message properties gives info about the afiliados availability,
            // price, author, number of pages, title etc
            putMsg = new MQMessage();
            putMsg.characterSet = 819;//MQC.MQCCSI_Q_MGR;
            putMsg.encoding = 273;
            putMsg.format = CMQC.MQFMT_STRING;
            //putMsg.write(messageString.getBytes());
            putMsg.writeString(messageString);

             // Specify the default put message options
            //MQPutMessageOptions pmo = new MQPutMessageOptions();
            // putting a message
            //   logger.info("Message [" + messageString + "]");
            //  logger.info("put");
            queueIn.put(putMsg);
            //logger.info("done");

            // accessing queue for both getting message
            //logger.info("Accessing queue " + queueNameOut + ".. ");
            queueOut = queueManager.accessQueue(queueNameOut, CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_FAIL_IF_QUIESCING);
            logger.info("done");

            // Now get the message back again. First define a WebSphere MQ
            // message
            // to receive the data
            // creating a message to get
            getMsg = new MQMessage();
            getMsg.characterSet = 819;//MQC.MQCCSI_Q_MGR;
            getMsg.encoding = 273;
            getMsg.format = CMQC.MQFMT_STRING;
            getMsg.messageId = putMsg.messageId;

            // Specify default get message options
            getMessageOptions = new MQGetMessageOptions();
            getMessageOptions.matchOptions = CMQC.MQMO_MATCH_MSG_ID;
            getMessageOptions.options = CMQC.MQGMO_WAIT;
            getMessageOptions.waitInterval = 3000;

            logger.info("get");

            int isContinuar = 0;
            int numReIntentos = 2;
            do {

                try {
                    // Get the message off the queue.					
                    queueOut.get(getMsg, getMessageOptions);
                    break;
                } catch (Exception e) {
                    //nothing
                    isContinuar++;
                    if (isContinuar <= numReIntentos) {
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
            }

        } catch (Exception e) {
            // TODO: handle exception
            throw e;

        } finally {

            //clear messages
            if (putMsg != null) {
                logger.info("Clear message put");
                putMsg.clearMessage();
            }

            if (getMsg != null) {
                logger.info("Clear message get");
                getMsg.clearMessage();
            }

            // Close the queues
            if (queueIn != null) {
                logger.info("Closing the queueIn");
                queueIn.close();
            }

            if (queueOut != null) {
                logger.info("Closing the queueOut");
                queueOut.close();
            }

            // Disconnect from the QueueManager
            if (queueManager != null) {

                logger.info("Disconnecting from the Queue Manager");
                queueManager.disconnect();
            }

        }
    }

}
