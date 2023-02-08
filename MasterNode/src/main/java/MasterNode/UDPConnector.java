package MasterNode;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

public class UDPConnector extends Thread {

    private int serverPort = 4711;
    private int dataLength = 1024;
    private ByteBuffer buffer = ByteBuffer.allocate(dataLength);
    private NodeManager nodeManager;

    // Performance test (throughput)
    private int messageCount = 0;
    private Date startDate = new Date();

    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(UDPConnector.class.getSimpleName());


    /* Constructor */
    public UDPConnector(Integer portNumber, NodeManager nodeManager) {

        LOGGER.info("Creating UDPConnector instance");

        this.serverPort = portNumber;
        this.nodeManager = nodeManager;
    }

    @Override
    public void run() {

        try {
            Selector selector = Selector.open();
            DatagramChannel channel = DatagramChannel.open();
            InetSocketAddress isa = new InetSocketAddress(this.serverPort);
            channel.socket().bind(isa);
            channel.configureBlocking(false);
            SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);

            LOGGER.info("UDPConnector instance listening on port " + this.serverPort + " for incoming datagrams");

            while (true) {
                try {
                    Integer numOfKeys = selector.select();
                    Iterator selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {

                        SelectionKey key = (SelectionKey) selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isReadable()) {
                            read(key);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.severe("error, continuing... " + (e.getMessage() != null ? e.getMessage() : ""));
                }
            }
        } catch (IOException e) {
            LOGGER.severe("network error: " + (e.getMessage() != null ? e.getMessage() : ""));
        }
    }


    private String getIPFromSocketAddress(SocketAddress socketAddress) {
        return socketAddress.toString().split(":")[0].substring(1);
    }

    private void read(SelectionKey key) throws IOException {
        try {
            DatagramChannel channel = (DatagramChannel) key.channel();
            SocketAddress socketAddress = channel.receive(this.buffer);

            String msg = new String(buffer.array());
            String IPAdress = getIPFromSocketAddress(socketAddress);

            JSONObject deserializedMessage = new JSONObject(msg);

            // Debug Print (Logging)
            LOGGER.info("Received message from host " + IPAdress
                    + " with length " + buffer.flip().remaining()
                    + " : " + deserializedMessage);


                // calculate packet transfer time
                String dateString = deserializedMessage.getString("date");
                Date packetDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(dateString);

                Date currentDate = new Date();
                Long transferTime = currentDate.getTime() - packetDate.getTime();

                LOGGER.info("Received Packet from '" + IPAdress + "'. Packet transfer time:  " + transferTime + "ms");


            this.persistInformation(msg);

            this.buffer.clear();

        } catch (ParseException p) {
            LOGGER.severe("Error when parsing date string: " + p.getMessage());

        } catch (JSONException j) {
            LOGGER.severe("Error when parsing JSON: " + j.getMessage());

        } finally {
            this.buffer.clear();
        }
    }

    private void persistInformation(String newMessage) {

        /* FOR TESTING PURPOSES */
        // Performance test (throughput)
//        this.messageCount++;
//        LOGGER.info("UDP PERFORMANCE TEST: " + this.messageCount + " of 4000 Messages received");
//
//        if (messageCount == 100) {
//            long diffMsec = (new Date().getTime()) - this.startDate.getTime();
//            LOGGER.info("UDP PERFORMANCE TEST FINISHED: 100 Messages received within " + diffMsec + "ms");
//            this.messageCount = 0;
//            this.startDate = new Date();
//        }


        JSONObject deserializedMessage = new JSONObject(newMessage);
        this.nodeManager.addNewValue(deserializedMessage);
    }
}
