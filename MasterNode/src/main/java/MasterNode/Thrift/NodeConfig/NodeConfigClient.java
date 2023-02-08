package MasterNode.Thrift.NodeConfig;

import MasterNode.HTMLResponseService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.logging.Logger;

public class NodeConfigClient {

  private String nodeName;
  private Integer nodePort;
  private TTransport transport; // RPC Transport Socket

  // Logger
  private static final Logger LOGGER = Logger.getLogger(NodeConfigClient.class.getSimpleName());

  //Constructor

  public NodeConfigClient(String nodeName, Integer nodePort) {
    this.nodeName = nodeName;
    this.nodePort = nodePort;


    try {
      this.transport = new TSocket(nodeName, nodePort);
      this.transport.open();
    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    }

  }

  // RPC Calls
  public boolean setNodeMaxValue(Integer newValue) {

    try {
      // Initialize RPC Connection
      NodeConfig.Client client = createNodeConfigClient(nodeName, nodePort);

      // Call RPC Procedure
      boolean result = client.setMax(newValue);
      this.transport.close();
      return result;

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean setNodeMinValue(Integer newValue) {

    try {
      // Initialize RPC Connection
      NodeConfig.Client client = createNodeConfigClient(nodeName, nodePort);

      // Call RPC Procedure
      boolean result = client.setMin(newValue);
      this.transport.close();
      return result;

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean turnNodeOff() {

    try {
      // Initialize RPC Connection
      NodeConfig.Client client = createNodeConfigClient(nodeName, nodePort);

      // Call RPC Procedure
      boolean result = client.turnOff();
      this.transport.close();
      return result;

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean turnNodeOn() {

    try {
      // Initialize RPC Connection
      NodeConfig.Client client = createNodeConfigClient(nodeName, nodePort);

      // Call RPC Procedure
      boolean result = client.turnOn();
      this.transport.close();
      return result;

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return false;
  }


  private NodeConfig.Client createNodeConfigClient(String nodeName, Integer nodePort) throws TTransportException {

    TProtocol protocol = new TBinaryProtocol(this.transport);
    return new NodeConfig.Client(protocol);

  }


}
