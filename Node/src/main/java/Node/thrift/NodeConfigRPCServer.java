package Node.thrift;

import Node.Node;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.logging.Logger;

public class NodeConfigRPCServer extends Thread {

  // RPC Modules
  private NodeConfigHandler nodeConfigHandler;
  private NodeConfig.Processor processor;

  // Node instance
  private Node node;

  // RPC Port
  private Integer port;

  // Logger
  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(NodeConfigRPCServer.class.getSimpleName());


  public NodeConfigRPCServer(Node node, Integer RPCPort){
    this.nodeConfigHandler = new NodeConfigHandler(node);
    this.processor = new NodeConfig.Processor(this.nodeConfigHandler);
    this.port = RPCPort;
  };

  @Override
  public void run() {
    TServerTransport serverTransport = null;
    try {
      serverTransport = new TServerSocket(this.port);
      TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

      LOGGER.info("Starting NodeConfig RPC-Server on Port " + this.port.toString());
      server.serve();

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    }


  }
}
