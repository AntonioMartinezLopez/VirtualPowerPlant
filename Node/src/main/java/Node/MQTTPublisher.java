package Node;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class MQTTPublisher {
  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MQTTPublisher.class.getSimpleName());

  private String clientName;
  private String brokerAddress;
  private MqttClient mqttClient;

  /**
   * Default constructor that initializes
   * various class attributes.
   */
  public MQTTPublisher(String clientName, String brokerAddress, String brokerPort) {
    this.clientName = clientName;
    // build MQTT broker address from protocol, address and port
    this.brokerAddress = "tcp://" + brokerAddress + ":" + brokerPort;
    this.connectToMqttBroker();
  }

  public void connectToMqttBroker() {
    // Setup MQTT connection
    try {
      // Create some MQTT connection options.
      MqttConnectOptions mqttConnectOpts = new MqttConnectOptions();
      mqttConnectOpts.setCleanSession(true);

      // Create the MQTT client
      this.mqttClient = new MqttClient(this.brokerAddress, this.clientName);

      // Connect to the MQTT broker using the connection options.
      this.mqttClient.connect(mqttConnectOpts);
      LOGGER.info("Connected to MQTT broker: " + this.mqttClient.getServerURI());
    } catch (MqttException e) {
      LOGGER.severe("An MQTT error occurred: " + e.getMessage());
    }
  }

  /**
   * Runs the MQTT client and publishes a message.
   */
  public void sendMqttMsg(String message, String topic) {
    if (!this.mqttClient.isConnected()) {
      LOGGER.info("MQTT connection is not set up yet. Setting up connection to: " + this.mqttClient.getServerURI());
      this.connectToMqttBroker();
    }

      try {
        // Create the message and set a quality-of-service parameter.
        /*
         * The at-most-once QoS parameter of MQTT:
         * QOS_AT_MOST_ONCE = 0;
         * The at-least-once QoS parameter of MQTT:
         * QOS_AT_LEAST_ONCE = 1;
         * The exactly-once QoS parameter of MQTT:
         * QOS_EXACTLY_ONCE = 2;
         */
        MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
        mqttMessage.setQos(0);

        // Publish the message.
        this.mqttClient.publish(topic, mqttMessage);
        LOGGER.info("Published message to topic '" + topic + "': " + message);

//      // Disconnect from the MQTT broker.
//      this.mqttClient.disconnect();
//      LOGGER.info("Disconnected from MQTT broker.");

      } catch (MqttException e) {
        LOGGER.severe("MQTT error occurred: " + e.getMessage());
      } catch (Exception e) {
        LOGGER.severe("EXCEPTION occurred: " + e.getMessage());
      }

  }



}
