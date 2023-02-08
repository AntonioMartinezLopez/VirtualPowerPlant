package Node;

import com.google.common.base.Defaults;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class UDPSocketClient {
    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(UDPSocketClient.class.getSimpleName());

    private InetAddress address;
    private Integer port;


    /**
     * Setup the UDP socket
     * @param ipAddress masterNode ip address
     * @param port masterNode port
     */
    public UDPSocketClient(String hostname, Integer port) {
        try {
            LOGGER.info("Setting up UDP connection to host " + hostname + " on Port " + port);
            this.address = InetAddress.getByName(hostname);
            this.port = port;
            LOGGER.info("Setup UDP connection to host " + this.address.getHostAddress() + " on Port " + this.port);
        } catch (UnknownHostException e) {
            LOGGER.severe("Can not parse the destination host address.\n"+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Method that transmits a String message via the UDP socket.
     *
     * This method is used to demonstrate the usage of datagram sockets
     * in Java. To this end, uses a try-with-resources statement that
     * closes the socket in any case of error.
     *
     *
     * @param msg The String message to transmit.
     */
    public void sendMsg(String msg) {
        // Create the UDP datagram socket.
        try (DatagramSocket udpSocket = new DatagramSocket()) {

            // Convert the message into a byte-array.
            byte[] buf = msg.getBytes();

            // Create a new UDP packet with the byte-array as payload.
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);

            // Send the data.
            udpSocket.send(packet);

            LOGGER.info("Message sent with payload: "+msg);
        } catch (SocketException e) {
            LOGGER.severe("Could not start the UDP socket server.\n" + e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.severe("Could not send data.\n" + e);
        }
    }

}
