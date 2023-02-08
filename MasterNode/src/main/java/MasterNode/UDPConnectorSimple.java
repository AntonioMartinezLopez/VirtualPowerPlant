package MasterNode;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

public class UDPConnectorSimple extends Thread {

    private int serverPort   = 4711;
    private int dataLength   = 1024;
    DatagramSocket socket = null;
    DatagramPacket inPacket, outPacket;
    byte[] data = new byte[ dataLength ];


    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(UDPConnectorSimple.class.getName());

    /* NodeManager */
    private NodeManager nodeManager;

    private int messageCount = 0;

    public UDPConnectorSimple(Integer portNumber, NodeManager nodeManager) {

        LOGGER.info("Creating UDPConnector instance");

        this.serverPort = portNumber;
        this.nodeManager = nodeManager;

        try {
            this.socket = new DatagramSocket(serverPort);
            LOGGER.info("UDPConnector server started");

        } catch (SocketException e) {
            LOGGER.severe("Could not create a socket for receiving UDP packets");
        }
    }

    @Override
    public void run(){

        while(true){


            try {

                inPacket = new DatagramPacket(data, data.length);
                socket.receive(inPacket);

                // decode request
                InetAddress IPadd = inPacket.getAddress();
                int port = inPacket.getPort();
                String line = new String(inPacket.getData());

                // Debug Print (Logging)
                LOGGER.info("Received message from: IP " + IPadd.toString()
                        + " port " + port
                        + " Data length " + inPacket.getLength()
                        + " : " + line );

                JSONObject deserializedMessage = new JSONObject(line);
                this.nodeManager.addNewValue(deserializedMessage);

            } catch (IOException e) {
                LOGGER.severe("Error in receiving UDP message: " + e.getMessage());
            }

            // FOR TESTING PURPOSES
            this.messageCount++;
            LOGGER.info("UDP PERFORMANCE TEST: " + this.messageCount + " of 4000 Messages received");
        }
    }
}
