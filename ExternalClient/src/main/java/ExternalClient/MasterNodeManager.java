package ExternalClient;

import ExternalClient.thrift.MasterNodeConfigClient;
import org.apache.thrift.TException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *  This class manages a group of NodeManagers belonging to different MasterNodes
 */
public class MasterNodeManager {
  HashMap<Integer, NodeManager> nodeManagers = new HashMap<>();
  Integer masterNodeRPCPort;
  Integer nodeRPCPort;

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(HTMLResponseService.class.getSimpleName());

  public MasterNodeManager(Integer masterNodeRPCPort, Integer nodeRPCPort) {
    this.masterNodeRPCPort = masterNodeRPCPort;
    this.nodeRPCPort = nodeRPCPort;
  }

  public void addNodeManager(Integer masterNodeId) {
    this.nodeManagers.put(masterNodeId, new NodeManager(masterNodeId, this.nodeRPCPort));
  }

  public boolean existsNodeManager(Integer masterNodeId) {
    return this.nodeManagers.containsKey(masterNodeId);
  }

  public NodeManager getNodeManager(Integer masterNodeId) {
    if (!existsNodeManager(masterNodeId)) {
      this.addNodeManager(masterNodeId);
    }
    return this.nodeManagers.get(masterNodeId);
  }

  public HashMap<Integer, NodeManager> getAllNodemanagers() {
    return this.nodeManagers;
  }

  /** RPC-Calls **/
  /**
   * Collects data from a given masterNodes via RPC calls.
   */
  public void collectReplicationData(int masterNodeId, int maxBlockSize){
    JSONObject replicationData = this.collectDataViaRPC(masterNodeId, maxBlockSize);
    this.importReplicationData(replicationData);
  }

  public JSONObject collectDataViaRPC(int masterNodeId, int maxBlockSize) {
    LOGGER.info("Trying to collect data via RPC on Port " + this.masterNodeRPCPort + " ...");
    LOGGER.info("Clearing List of existing NodeManagers before fetching new data");
    this.nodeManagers.clear();

    try {
      // create RPC client
      String masterNodeHostname = "masternode-" + masterNodeId;
      MasterNodeConfigClient masterNodeConfigClient = new MasterNodeConfigClient(masterNodeHostname, this.masterNodeRPCPort);
      LOGGER.info("Created MasterNodeConfigClient with hostname " + masterNodeHostname + " and port " + this.masterNodeRPCPort);

      int streamId = masterNodeConfigClient.OpenStream();
      if (streamId != -1) {
        LOGGER.info("RPC connected. StreamId = " + streamId);

        // initialize message buffer
        byte[] emptyBuffer = new byte[0];
        ByteBuffer msg = ByteBuffer.wrap(emptyBuffer);

        // collect payload packets from server
        boolean receivedLastRequest = false;
        while (!receivedLastRequest){

          MasterNodeConfigClient masterNodeConfigClient2 = new MasterNodeConfigClient(masterNodeHostname, this.masterNodeRPCPort);

          // read next payload block
          ByteBuffer payload = masterNodeConfigClient2.ReadNextBlock(streamId, maxBlockSize);

          // print received text block
          String s = StandardCharsets.UTF_8.decode(payload).toString();
          LOGGER.info("Received payload block: " + s);

          // rewind payload
          payload.rewind();

          // Reassemble message from payload blocks
          ByteBuffer temp = ByteBuffer.allocate(msg.limit() + payload.limit());
          temp.put(msg);
          temp.put(payload);
          temp.rewind();
          msg = temp;
          msg.rewind();

          // check whether received block has length 0
          payload.flip();
          if(payload.remaining() == 0) {
            receivedLastRequest = true;
            LOGGER.info("Received last payload block.");
          }
        }

        // print reassembled message
        String reassembledMessage = StandardCharsets.UTF_8.decode(msg).toString();
        LOGGER.info("Reassembled message: " + reassembledMessage);
        msg.rewind();

        // convert reassembled message to JSON
        JSONObject result = new JSONObject(reassembledMessage);
        return result;

      } else {
        LOGGER.severe("RPC Connection failed!");
      }
    } catch (TException t) {
      LOGGER.severe("Error when trying to collect data via RPC: " + t.getMessage());
    }
    return null;
  }

  public void importReplicationData(JSONObject replicationData) {
    for(String masterNodeId : replicationData.keySet()){
      NodeManager nodeManager = this.getNodeManager(Integer.valueOf(masterNodeId));
      JSONObject nodeData = replicationData.getJSONObject(masterNodeId);
      for(String nodeId : nodeData.keySet()){
        nodeManager.importNodeData(nodeData.getJSONObject(nodeId));
        nodeManager.setHealthCheckerStatusCode(-2);
      }
      this.nodeManagers.put(Integer.valueOf(masterNodeId), nodeManager);
    }
  }

  public ArrayList<Integer> getAllMasterNodesIDs() {
    return new ArrayList<>(this.nodeManagers.keySet());
  }

}
