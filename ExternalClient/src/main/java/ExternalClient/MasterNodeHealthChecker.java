package ExternalClient;

import ExternalClient.thrift.MasterNodeConfigClient;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class MasterNodeHealthChecker extends Thread {
  private Integer healthCheckRate;
  private String[] masterNodeIds;
  private MasterNodeManager masterNodeManager;
  private Integer RPCPort;

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MasterNodeHealthChecker.class.getSimpleName());


  public MasterNodeHealthChecker(MasterNodeManager masterNodeManager, String[] masterNodeIds, Integer healthCheckRate, Integer RPCPort){
    this.healthCheckRate = healthCheckRate;
    this.masterNodeIds = masterNodeIds;
    this.masterNodeManager = masterNodeManager;
    this.RPCPort = RPCPort;
  }


  @Override
  public void run() {
    LOGGER.info("Starting NodeHealthChecker Service");
    while (true) {
      try {
        Thread.sleep((this.healthCheckRate) * 1000);
      } catch(InterruptedException ie) {}

      this.checkMasterNodeHealth();
    }
  }

  /**
   * check status of each known MasterNode.
   */
  public void checkMasterNodeHealth() {

    for (String masterNodeIdString: this.masterNodeIds) {
      Integer masterNodeId = Integer.valueOf(masterNodeIdString);
      Integer healthStatus = -2;
      try {

        // Create RPC-Client for masterNode
        MasterNodeConfigClient masterNodeConfigClient = new MasterNodeConfigClient("masternode-"+masterNodeId, this.RPCPort, 1000);

        // Check health status of respective masterNode
        healthStatus = masterNodeConfigClient.getHealthStatus();

      } catch (TException e) {
        // if a MasterNode does not respond, mark it as unhealthy
        LOGGER.severe("getHealthStatus(): MasterNode " + masterNodeId + " is not responding. Returning statuscode '-1'");
        healthStatus = -1;

      } finally {

        // handle status codes
        switch (healthStatus) {
          // this switch case can be extended further, if more status codes are returned
          case -1: {
            // if MasterNode is not responding via RPC
            LOGGER.severe("MasterNode FAILURE detected: masterNode-" + masterNodeId);
            break;
          }
          case 1: {
            // if MasterNode is healthy and returns a status code of 1
            LOGGER.info("MasterNode " + masterNodeId + " is HEALTHY");
            break;
          }
        }

        // save status code in corresponding NodeManager
        NodeManager nodeManager = this.masterNodeManager.getNodeManager(masterNodeId);
        nodeManager.setHealthCheckerStatusCode(healthStatus);
      }
    }
  }
}
