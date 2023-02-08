package MasterNode.Thrift.MasterNodeConfig;

import MasterNode.MasterNodeManager;
import MasterNode.NodeEntity;
import MasterNode.NodeManager;
import org.apache.thrift.TException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MasterNodeConfigHandler implements MasterNodeConfig.Iface {

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MasterNodeConfigHandler.class.getSimpleName());

  private MasterNodeManager masterNodeManager;
  private int maxStream; // Amount of maximum opened streams

  private HashMap<Integer,byte[]> streamDataMap;

  public MasterNodeConfigHandler (MasterNodeManager masterNodeManager) {

    this.masterNodeManager = masterNodeManager;
    this.streamDataMap = new HashMap<>();
    this.maxStream = 10;
  }


  @Override
  public int OpenStream() throws TException {

    LOGGER.info("RPC function 'OpenStream()' called.");

    // Collect client data so we can identify and assign a stream id
    int streamId = -1;
    // search for a unused stream id
    for (int i = 0; i < this.maxStream; i++){
      if(!this.streamDataMap.containsKey(i)){
        streamId = i;
      }
    }

    // Call NodeManager function to generate JSON-Object of complete history
    // collect nodes and their history data from nodemanager
    try {

      //Initialize final JSON Object
      JSONObject finalJSON = new JSONObject();

      // get hash map of all NodeManagers
      HashMap<Integer, NodeManager> nodeManagersHashMap = this.masterNodeManager.getAllNodemanagers();

      // collect all NodeEntities from NodeManagers
      for(Integer masterNodeId : nodeManagersHashMap.keySet()) {
        JSONObject allNodesJson = new JSONObject();
        NodeManager nodeManager = nodeManagersHashMap.get(masterNodeId);
        ArrayList<NodeEntity> allNodes = nodeManager.getAllNodes();
        if (!allNodes.isEmpty()) {
          for (NodeEntity node : allNodes) {
            allNodesJson.put(node.getNodeID(), node.getJSON());
          }

          //assemble to final JSON Object
          finalJSON.put(masterNodeId.toString(), allNodesJson);

        } else {
          allNodesJson.put(masterNodeId.toString(), "No Nodes found in Nodemanager!");
        }
      }

      // Save Byte-Array and return corresponding index
      byte[] byteJson = finalJSON.toString().getBytes(StandardCharsets.UTF_8);
      this.streamDataMap.put(streamId, byteJson);

      return streamId;

    } catch (JSONException e){
      LOGGER.severe(e.getMessage());
    }

    // Serialize JSON-Object and convert to Byte-Array
//    allNodesJson.toString();
    return -1;
  }

  @Override
  public ByteBuffer ReadNextBlock(int StreamId, int maxBlockSize) throws TException {
    LOGGER.info("RPC function 'ReadNextBlock()' called.");
    try {

      // if we don't have any more data to send, close the stream and send an empty ByteBuffer
      if (this.streamDataMap.get(StreamId).length == 0) {
        LOGGER.info("ReadNextBlock(): No more payload to send. Closing stream.");
        // close the stream
        this.streamDataMap.remove(StreamId);

        // send empty ByteBuffer
        return ByteBuffer.wrap(new byte[0]);
      }

      // get remaining byte-array of requested StreamId from streamDataMap
      byte[] byteJson = this.streamDataMap.get(StreamId);

      // copy payload block into new variable
      byte[] payload = Arrays.copyOfRange(byteJson, 0, maxBlockSize);

      // remove payload block from original byte array
      if (maxBlockSize >= byteJson.length) {
        // if the maxBlockSize is bigger than our remaining array size, send the last payload package and set our byteJson array length to 0
        byteJson = new byte[0];
        this.streamDataMap.put(StreamId, byteJson);
        return ByteBuffer.wrap(payload);
      } else {
        byteJson = Arrays.copyOfRange(byteJson, maxBlockSize, byteJson.length+1);
      }

      this.streamDataMap.put(StreamId, byteJson);

      // send payload block
      LOGGER.info("ReadNextBlock(): sending payload ...");
      return ByteBuffer.wrap(payload);

    } catch (ArrayIndexOutOfBoundsException ae) {
      LOGGER.severe("ArrayIndexOutOfBoundsException: " + ae.getStackTrace());
    } catch (NullPointerException ne) {
      LOGGER.severe("NullPointerException: " + ne.getStackTrace());
    } catch (IllegalArgumentException iae) {
      LOGGER.severe("IllegalArgumentException: " + iae.getStackTrace());
    } catch (Exception e) {
      LOGGER.severe("GENERAL EXCEPTION: " + e.getStackTrace());
    }

    return null;
  }

  @Override
  public int getHealthStatus() throws TException {
//    LOGGER.info("RPC function 'getHealthStatus()' called.");
    // Simply return 1 when masternode is alive
    // This function can later be used to return different status codes
    return 1;
  }
}