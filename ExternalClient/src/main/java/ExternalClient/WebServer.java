package ExternalClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class WebServer extends Thread {

  private Integer port = 8080;
  private MasterNodeManager masterNodeManager;
  private String[] masterNodeIds;

  public WebServer(MasterNodeManager masterNodeManager, String[] masterNodeIds) {
    this.masterNodeManager = masterNodeManager;
    this.masterNodeIds = masterNodeIds;
  }

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(WebServer.class.getSimpleName());


  @Override
  public void run() {

    try {
      ServerSocket listenSocket = new ServerSocket(this.port);
      LOGGER.info("Multithreaded Webserver starts on Port " + this.port);
      while (true) {
        Socket client = listenSocket.accept();
        new HTMLResponseService(client, this.masterNodeManager, masterNodeIds).start();
      }

    } catch (IOException e) {
      LOGGER.severe("Webserver socket error detected. Abort. " + e.getMessage());
    }

  }

}