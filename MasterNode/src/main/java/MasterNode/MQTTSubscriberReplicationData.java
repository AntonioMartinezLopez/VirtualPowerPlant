package MasterNode;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.logging.Logger;

public class MQTTSubscriberReplicationData {
  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MQTTSubscriberReplicationData.class.getSimpleName());

  private Integer masterNodeId;
  private MasterNodeManager masterNodeManager;
  private String clientName;
  private String brokerAddress;
  private String brokerPort;
  private String brokerURL;

  public MQTTSubscriberReplicationData(Integer masterNodeId, String clientName, String brokerAddress, String brokerPort, MasterNodeManager masterNodeManager) {
    this.masterNodeId = masterNodeId;
    this.clientName = clientName;
    this.masterNodeManager = masterNodeManager;
    // build MQTT broker address from protocol, address and port
    this.brokerAddress = brokerAddress;
    this.brokerPort = brokerPort;
    this.brokerURL = "tcp://" + brokerAddress + ":" + brokerPort;
  }

  public void startMQTTMsgCollection(String topic) {
    try {
      MqttClient client = new MqttClient(brokerURL, clientName);
      client.setCallback(new MQTTSubscriberCallbackReplicationData(this.masterNodeId, masterNodeManager));

      // Connect to the MQTT broker.
      client.connect();
      LOGGER.info("Connected to MQTT broker: " + client.getServerURI());

      // Subscribe to a topic.
      client.subscribe(topic);
      LOGGER.info("Subscribed to topic: " + topic);

    } catch (MqttException e) {
      LOGGER.severe("An error occurred: " + e.getMessage());
    }
  }


}
