package MasterNode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class WebServer extends Thread {

    private Integer masterNodeId;
    private Integer port;
    private NodeManager nodeManager;

    public WebServer(Integer masterNodeId, NodeManager nodeManager) {
        this.masterNodeId = masterNodeId;
        //build port from id
        String webserverPort = "808" + masterNodeId;
        this.port = Integer.parseInt(webserverPort);
        this.nodeManager = nodeManager;
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
                new HTMLResponseService(client, nodeManager, this.masterNodeId).start();
            }

        } catch (IOException e) {
            LOGGER.severe("Webserver socket error detected. Abort. " + e.getMessage());
        }

    }

}
