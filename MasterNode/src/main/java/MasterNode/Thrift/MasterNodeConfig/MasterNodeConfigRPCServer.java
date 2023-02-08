package MasterNode.Thrift.MasterNodeConfig;

import MasterNode.MasterNodeManager;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.logging.Logger;

public class MasterNodeConfigRPCServer extends Thread {

  // RPC Modules
  private MasterNodeConfigHandler masterNodeConfigHandler;
  private MasterNodeConfig.Processor processor;

  // RPC Port
  private Integer port;


  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(MasterNodeConfigRPCServer.class.getSimpleName());


  public MasterNodeConfigRPCServer(MasterNodeManager masterNodeManager, Integer RPCPort){
    this.masterNodeConfigHandler = new MasterNodeConfigHandler(masterNodeManager);
    this.processor = new MasterNodeConfig.Processor(this.masterNodeConfigHandler);
    this.port = RPCPort;
  };

  @Override
  public void run() {
    TServerTransport serverTransport = null;
    try {
      serverTransport = new TServerSocket(this.port);
//      TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

      // use multithreaded RPC-Server
      TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

      LOGGER.info("Starting MasterNodeConfig RPC-Server on Port " + this.port.toString());
      server.serve();

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    }


  }
}
