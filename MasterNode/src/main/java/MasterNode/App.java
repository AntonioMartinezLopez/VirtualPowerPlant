/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package MasterNode;

import MasterNode.Thrift.MasterNodeConfig.MasterNodeConfigRPCServer;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class App {

    /* Logger */
    private static final Logger LOGGER = Logger.getLogger("MAIN");

    /* Format Logger output */
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT][%4$-7s][%3$-19s] %5$s %n");
    }


    public static void main(String[] args) {

        try {
            // Collect environment variables
            Integer masterNodeId        = Integer.valueOf(System.getenv("MASTERNODEID"));
            Integer healthCheckTimeout  = Integer.valueOf(System.getenv("HEALTHCHECKTIMEOUT"));
            Integer nodeRpcPort         = Integer.valueOf(System.getenv("NODERPCPORT"));
            Integer masterNodeRpcPort   = Integer.valueOf(System.getenv("MASTERNODERPCPORT"));
            String mqttBrokerAddress    = System.getenv("MQTTBROKERADDRESS");
            String mqttBrokerPort       = System.getenv("MQTTBROKERPORT");
            String[] otherMasterNodeIds = System.getenv("OTHERMASTERNODEIDS").split(",");

            String connectionType       = System.getenv("CONNECTIONTYPE");


            NodeManager ownNodeManager = new NodeManager(masterNodeId, nodeRpcPort);
            MasterNodeManager masterNodeManager = new MasterNodeManager(masterNodeId, ownNodeManager, nodeRpcPort);

            // Either setup UDP or MQTT connection
            if (!connectionType.isEmpty()) {
                if (connectionType.equals("UDP")) {
                    LOGGER.info("Setting up connection type UDP\n");
                    Integer masterNodePort      = Integer.valueOf(System.getenv("MASTERNODEPORT"));
                    /* Start the NIO-Selector based  UDP-Connector */
                    UDPConnector UDPConnector = new UDPConnector(masterNodePort, masterNodeManager.getNodeManager(masterNodeId));
                    UDPConnector.start();

                    /* Start the simple UDP-Connector */
                    //UDPConnectorSimple simple = new UDPConnectorSimple(masterNodePort, nodeManager);
                    //simple.start();

                } else if (connectionType.equals("MQTT")) {
                    LOGGER.info("Setting up connection type MQTT\n");

                    String mqttMsgTopic         = "masternode-" + masterNodeId + "-incoming";
                    /* Setup MQTT broker connection for own nodes */
                    String clientName = "masternode-" + masterNodeId;
                    MQTTSubscriberOwnData mqttSubscriberOwnData = new MQTTSubscriberOwnData(masterNodeId, clientName, mqttBrokerAddress, mqttBrokerPort, masterNodeManager.getNodeManager(masterNodeId));
                    mqttSubscriberOwnData.startMQTTMsgCollection(mqttMsgTopic);

                    if (otherMasterNodeIds.length != 0) {
                        LOGGER.info("Setting up replication services ... \n");
                        /* Setup MQTT broker connection for receiving replication data */
                        for (String otherMasterNodeId : otherMasterNodeIds) {
                            int id = Integer.parseInt(otherMasterNodeId);

                            // skip own masterNodeId
                            if (id == masterNodeId) {
                                continue;
                            }

                            LOGGER.info("Setting up replication services with masternode-" + id + "\n");
                            String clientNameReplication = "masternode-" + masterNodeId + "-replicationClient";
                            MQTTSubscriberReplicationData mqttSubscriberReplicationData = new MQTTSubscriberReplicationData(id, clientNameReplication, mqttBrokerAddress, mqttBrokerPort, masterNodeManager);
                            String topic = "masternode-" + id + "-outgoing";
                            mqttSubscriberReplicationData.startMQTTMsgCollection(topic);
                        }
                    }
                } else {
                    LOGGER.severe("Unknown connection type! Please provide a valid connection type (UDP or MQTT).\n");
                    System.exit(1);
                }
            }

            /* Start the Webserver for the client */
            WebServer webServer = new WebServer(masterNodeId, masterNodeManager.getNodeManager(masterNodeId));
            webServer.start();

            /* Start the HealthChecker instance */
            NodeHealthChecker healthChecker = new NodeHealthChecker(masterNodeManager.getNodeManager(masterNodeId), healthCheckTimeout);
            healthChecker.start();

            // Create MasterNodeConfig and start RPC-Server
            MasterNodeConfigRPCServer masterNodeConfigServer = new MasterNodeConfigRPCServer(masterNodeManager, masterNodeRpcPort);
            masterNodeConfigServer.start();

        } catch (NumberFormatException ne) {
            LOGGER.severe("Missing environment variables! Please provide all necessary environment variables.\n" + ne.getMessage());
            System.exit(1);
        }
    }
}