package MasterNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  This class manages a group of NodeManagers belonging to different MasterNodes
 */
public class MasterNodeManager {
  Integer ownMasterNodeId;
  HashMap<Integer, NodeManager> nodeManagers = new HashMap<>();
  Integer rpcPort;

  public MasterNodeManager(Integer ownMasterNodeId, NodeManager ownNodeManager, Integer rpcPort) {
    this.ownMasterNodeId = ownMasterNodeId;
    this.nodeManagers.put(ownMasterNodeId, ownNodeManager);
    this.rpcPort = rpcPort;
  }

  public void addNodeManager(Integer masterNodeId, NodeManager nodeManager) {
    this.nodeManagers.put(masterNodeId, nodeManager);
  }

  public boolean existsNodeManager(Integer masterNodeId) {
    return this.nodeManagers.containsKey(masterNodeId);
  }

  public NodeManager getNodeManager(Integer masterNodeId) {
    if (existsNodeManager(masterNodeId)) {
      return this.nodeManagers.get(masterNodeId);
    } else {
      NodeManager nodeManager = new NodeManager(masterNodeId, this.rpcPort);
      this.addNodeManager(masterNodeId, nodeManager);
      return nodeManager;
    }
  }

  public HashMap<Integer, NodeManager> getAllNodemanagers() {
    return this.nodeManagers;
  }
}
