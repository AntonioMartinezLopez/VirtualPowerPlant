package Node.thrift;

import Node.Node;
import org.apache.thrift.TException;

import java.util.logging.Logger;

public class NodeConfigHandler implements NodeConfig.Iface {

  private Node node;

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(NodeConfigHandler.class.getSimpleName());

  public NodeConfigHandler(Node node) {
    this.node = node;
  }

  @Override
  public boolean setMax(int newValue) throws TException {
    LOGGER.info("MaxValue of Node " + node.getNodeName() + " with ID " + node.getNodeId() + " was set to " + newValue);
    node.setValueMax(newValue);
    return true;
  }

  @Override
  public boolean setMin(int newValue) throws TException {
    LOGGER.info("MinValue of Node " + node.getNodeName() + " with ID " + node.getNodeId() + " was set to " + newValue);
    node.setValueMin(newValue);
    return true;
  }

  @Override
  public boolean turnOff() throws TException {
    LOGGER.info("Node " + node.getNodeName() + " with ID " + node.getNodeId() + " was turned off");
    this.node.setRunning(false);
    return true;
  }

  @Override
  public boolean turnOn() throws TException {
    LOGGER.info("Node " + node.getNodeName() + " with ID " + node.getNodeId() + " was turned on");
    this.node.setRunning(true);
    return true;
  }
}
