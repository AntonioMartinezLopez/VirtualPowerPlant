package MasterNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class NodeHealthChecker extends Thread {

  private Integer timeout;
  private NodeManager nodeManager;

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(NodeHealthChecker.class.getSimpleName());


  public NodeHealthChecker(NodeManager nodeManager, Integer healthCheckTimeout){
    this.timeout = healthCheckTimeout;
    this.nodeManager = nodeManager;
  }


  @Override
  public void run() {
    LOGGER.info("Starting NodeHealthChecker Service");
    while (true) {
      try {
        Thread.sleep(5000);
      } catch(InterruptedException ie) {}

      this.checkNodeHealth();
    }
  }

  /**
   * check status of each known node.
   * When last Value is older than timeout value, a node is unhealty
   */
  public void checkNodeHealth() {
    ArrayList<String> allNodes = this.nodeManager.getAllNodesID();
    for (String nodeID: allNodes
         ) {
      try {
        NodeEntity node = this.nodeManager.getSingleNode(nodeID);
        NodeData nodeData = this.nodeManager.getLatestNodeData(nodeID);
        Date currentDate = new Date();
        if ((currentDate.getTime() - nodeData.getDate().getTime()) > (this.timeout * 1000L)) {
          node.setStatus("unhealthy");
          LOGGER.warning("Node " + node.getName() + " (ID: " + nodeID + ") is set unhealthy. Reason: Timeout value of " + this.timeout + " was exceeded.");
        }
      } catch (NodeNotFoundException e){
        LOGGER.severe(e.toString());
      }
    }
  }
}
