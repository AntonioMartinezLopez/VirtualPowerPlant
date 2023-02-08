package Node;

import org.json.JSONObject;

import java.util.*;
import java.util.logging.*;

public class Node extends Thread {
    /* Randomizer object */
    Random random = new Random();

    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(Node.class.getSimpleName());

    private String id ;
    private Character type;
    private String name;
    private String connectionType;
    private boolean running;
    private Integer currentValue;
    private Integer valueMax;
    private Integer valueMin;
    private String masterNodeHostname;
    private Integer masterNodePort;
    private Integer sendRate;
    private Integer corruptDataRate;
    private Integer validPacketCounter = 0;
    private String mqttMsgTopic;

    private UDPSocketClient UDPSocket;

    private MQTTPublisher MQTTPublisher;

    /** UDP Constructor */
    public Node(
            String id,
            Character type,
            String name,
            String connectionType,
            Integer valueMin,
            Integer valueMax,
            Integer masterNodeId,
            Integer masterNodePort,
            Integer sendrate,
            Integer corruptDataRate) {
        this.id = id;
        this.type = Character.toLowerCase(type);
        this.name = name;
        this.connectionType = connectionType;
        this.currentValue = random.nextInt((valueMax - valueMin) + 1 ) + valueMin;
        this.valueMax = valueMax;
        this.valueMin = valueMin;
        this.masterNodeHostname = "masternode-" + masterNodeId;
        this.masterNodePort = masterNodePort;
        this.sendRate = sendrate;
        this.corruptDataRate = corruptDataRate;
        this.running = true;

        this.UDPSocket = new UDPSocketClient(this.masterNodeHostname, this.masterNodePort);
    }

    /** MQTT Constructor */
    public Node (
            String id,
            Character type,
            String name,
            String connectionType,
            Integer valueMin,
            Integer valueMax,
            Integer masterNodeId,
            Integer sendrate,
            Integer corruptDataRate,
            String mqttBrokerAddress,
            String mqttBrokerPort) {
        this.id = id;
        this.type = Character.toLowerCase(type);
        this.name = name;
        this.connectionType = connectionType;
        this.currentValue = random.nextInt((valueMax - valueMin) + 1 ) + valueMin;
        this.valueMax = valueMax;
        this.valueMin = valueMin;
        this.masterNodeHostname = "masternode-" + masterNodeId;
        this.sendRate = sendrate;
        this.corruptDataRate = corruptDataRate;
        this.running = true;

        this.mqttMsgTopic = "masternode-" + masterNodeId + "-incoming";
        this.MQTTPublisher = new MQTTPublisher(this.name, mqttBrokerAddress, mqttBrokerPort);
    }

    // Getter

    /**
     * Simple getter for currentValue.
     * Does NOT generate a new randomized value!
     * @return Integer currentValue
     */
    private Integer getCurrentValue() {
        return this.currentValue;
    }

    public String getNodeId() {
        return id;
    }

    public Character getType() {
        return type;
    }

    public String getNodeName() {
        return name;
    }

    public boolean getStatus() {
        return running;
    }


    // Setter

    public void setValueMax(Integer valueMax) {
        this.valueMax = valueMax;
    }

    public void setValueMin(Integer valueMin) {
        this.valueMin = valueMin;
    }

    public void setRunning(boolean status) {
        this.running = status;
    }

    // Methods

    /**
     * Generates a new randomized value and pushes it to the nodes history.
     * @return Integer randomizedValue
     */
    public Integer generateNewValue() {

        //If Node is turned on (= running)
        if(this.running) {
            int randomizedValue = random.nextInt((valueMax - valueMin) + 1) + valueMin;
            this.currentValue = randomizedValue;
            return randomizedValue;
        }
        // If Node is turned off then return 0
        return 0;
    }

    /**
     * creates a json object with node data.
     * the json object will then be sent to the masterNode.
     */
    public void sendData() {
        JSONObject json= new JSONObject();
        Date date = new Date();


        String status = "online";
        // convert boolean value from status to string
        if (!this.running) {status = "offline";};


        // add node data to json
        json.put("id", this.getNodeId());
        json.put("date", date);
        json.put("type", this.getType());
        json.put("name", this.getNodeName());
        json.put("status", status);
        json.put("currentValue", this.generateNewValue());

        // send json object to masterNode depending on connection type
        if (this.connectionType.equals("UDP")) {
            this.UDPSocket.sendMsg(json.toString());
        } else if (this.connectionType.equals("MQTT")) {
            this.MQTTPublisher.sendMqttMsg(json.toString(), this.mqttMsgTopic);
        } else {
            LOGGER.severe("Cannot send data because connection type is unknown! Please provide a valid connection type (UDP or MQTT).\n");
        }
    }

    public void sendCorruptData() {
        // send corrupt data that is not in json format to masterNode
        if (this.connectionType.equals("UDP")) {
            this.UDPSocket.sendMsg("CORRUPTDATA");
        } else if (this.connectionType.equals("MQTT")) {
            this.MQTTPublisher.sendMqttMsg("CORRUPTDATA", this.mqttMsgTopic);
        } else {
            LOGGER.severe("Cannot send (corrupt) data because connection type is unknown! Please provide a valid connection type (UDP or MQTT).\n");
        }
    }

    /**
     * main worker loop of the node.
     * uses the nodes sendrate to send data to to masterNode accordingly.
     */
    @Override
    public void run() {
        /* FOR TESTING PURPOSES */
//        int counter = 1;

        while (true) {
            try {
                /* FOR TESTING */
                //Thread.sleep(this.sendRate * 1);
                Thread.sleep(this.sendRate * 1000);
            } catch(InterruptedException ie) {}

            if (this.corruptDataRate > 0) {
                if (this.validPacketCounter < this.corruptDataRate) {
                    sendData();
                    validPacketCounter++;
                } else {
                    sendCorruptData();
                    this.validPacketCounter = 0;
                }
            } else {
                sendData();
                validPacketCounter++;
            }

            /* FOR TESTING PURPOSES */
//            LOGGER.info("SENT MESSAGES: " + counter);
//            if(counter++ == 1000){
//                break;
//            }
        }
    }
}
