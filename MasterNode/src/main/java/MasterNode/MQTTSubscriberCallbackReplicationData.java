package MasterNode;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

public class MQTTSubscriberCallbackReplicationData implements MqttCallback {
  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MQTTSubscriberCallbackReplicationData.class.getSimpleName());

  private MasterNodeManager masterNodeManager;
  private Integer masterNodeId;


  // Performance test (throughput)
  private int messageCount = 0;
  private Date startDate = new Date();

  public MQTTSubscriberCallbackReplicationData(
          Integer masterNodeId,
          MasterNodeManager masterNodeManager
  ) {
    this.masterNodeId = masterNodeId;
    this.masterNodeManager = masterNodeManager;
  }

  @Override
  public void connectionLost(Throwable throwable) {
    LOGGER.severe("Connection to MQTT broker lost!");
  }

  @Override
  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    LOGGER.info("Message received from topic '" + s + "': " + new String(mqttMessage.getPayload()));
    try {
      JSONObject deserializedMessage = new JSONObject(new String(mqttMessage.getPayload()));
      this.masterNodeManager.getNodeManager(this.masterNodeId).addNewValue(deserializedMessage);

      /* FOR TESTING PURPOSES */
      //Performance test (throughput)
//      this.messageCount++;
//      LOGGER.info("MQTT PERFORMANCE TEST: " + this.messageCount + " of 4000 Messages received");
//
//      if (messageCount == 100) {
//        long diffMsec = (new Date().getTime()) - this.startDate.getTime();
//        LOGGER.info("MQTT PERFORMANCE TEST FINISHED: 100 Messages received within " + diffMsec + "ms");
//        this.messageCount = 0;
//        this.startDate = new Date();
//      }

    } catch (JSONException j) {
      LOGGER.severe("Error when parsing JSON: " + j.getMessage());
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) {
    try {
      LOGGER.info("Delivery completed: "+ mqttDeliveryToken.getMessage() );
    } catch (MqttException e) {
      LOGGER.severe("Failed to get delivery token message: " + e.getMessage());
    }
  }

}
