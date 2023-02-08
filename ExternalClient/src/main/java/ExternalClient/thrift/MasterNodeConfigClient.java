package ExternalClient.thrift;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class MasterNodeConfigClient {

  // Logger
  private static final Logger LOGGER = Logger.getLogger(MasterNodeConfigClient.class.getSimpleName());

  private String masterNodeName;
  private Integer masterNodePort;
  private TTransport transport; // RPC Transport Socket

  // Constructor with default RPC timeout
  public MasterNodeConfigClient(String masterNodeName, Integer masterNodePort) {
    this.masterNodeName = masterNodeName;
    this.masterNodePort = masterNodePort;

    try {
      this.transport = new TSocket(masterNodeName, masterNodePort);
      this.transport.open();
    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    }
  }

  // Second Constructor with custom RPC timeout
  public MasterNodeConfigClient(String masterNodeName, Integer masterNodePort, Integer timeout) {
    this.masterNodeName = masterNodeName;
    this.masterNodePort = masterNodePort;

    try {
      // generate a custom TSocket, so we can use a custom timeout value
      TConfiguration tConfig = new TConfiguration();
      this.transport = new TSocket(tConfig, masterNodeName, masterNodePort, timeout);
      this.transport.open();
    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    }
  }

  private MasterNodeConfig.Client createMasterNodeConfigClient() throws TTransportException {

    TProtocol protocol = new TBinaryProtocol(this.transport);
    return new MasterNodeConfig.Client(protocol);

  }

  // RPC Calls
  public int OpenStream() throws TException {
    try {
      // Initialize RPC Connection
      MasterNodeConfig.Client client = createMasterNodeConfigClient();

      // Call RPC Procedure
      int streamId = client.OpenStream();
      this.transport.close();
      LOGGER.info("streamId: " + streamId);
      return streamId;


    } catch (TTransportException e) {
      LOGGER.severe("Error in OpenStream(): " + e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public ByteBuffer ReadNextBlock(int StreamId, int maxBlockSize) throws TException {
    try {
      LOGGER.info("ReadNextBlock(StreamId= " + StreamId + ", maxBlockSize= " + maxBlockSize + ")");
      // Initialize RPC Connection
      MasterNodeConfig.Client client = createMasterNodeConfigClient();

      // Call RPC Procedure
      ByteBuffer buffer = client.ReadNextBlock(StreamId, maxBlockSize);
      this.transport.close();
      LOGGER.info("ReadNextBlock(): Returning buffer: " + buffer);
      return buffer;

    } catch (TTransportException e) {
      LOGGER.severe(e.toString());
    } catch (TException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int getHealthStatus() throws TException {
    // Initialize RPC Connection
    MasterNodeConfig.Client client = createMasterNodeConfigClient();

    // Call RPC Procedure
    int healthStatus = client.getHealthStatus();
    this.transport.close();

    return healthStatus;
  }
}
