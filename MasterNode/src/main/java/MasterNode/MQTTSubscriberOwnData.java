package MasterNode;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.logging.Logger;

public class MQTTSubscriberOwnData {
  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MQTTSubscriberOwnData.class.getSimpleName());

  private Integer masterNodeId;
  private NodeManager nodeManager;
  private String clientName;
  private String brokerAddress;
  private String brokerPort;
  private String brokerURL;

  public MQTTSubscriberOwnData(Integer masterNodeId, String clientName, String brokerAddress, String brokerPort, NodeManager nodeManager) {
    this.masterNodeId = masterNodeId;
    this.clientName = clientName;
    this.nodeManager = nodeManager;
    // build MQTT broker address from protocol, address and port
    this.brokerAddress = brokerAddress;
    this.brokerPort = brokerPort;
    this.brokerURL = "tcp://" + brokerAddress + ":" + brokerPort;
  }

  public void startMQTTMsgCollection(String topic) {
    try {
      MqttClient client = new MqttClient(brokerURL, clientName);
      client.setCallback(new MQTTSubscriberCallbackOwnData(this.masterNodeId, nodeManager, this.brokerAddress, this.brokerPort));

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
