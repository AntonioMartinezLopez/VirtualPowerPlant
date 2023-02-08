package MasterNode;

import MasterNode.Thrift.NodeConfig.NodeConfigClient;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HTMLResponseService extends Thread {

    private Socket client;
    private NodeManager nodeManager;
    private Integer masterNodeId;

    public HTMLResponseService(Socket client, NodeManager nodeManager, Integer masterNodeId) {
        this.client = client;
        this.nodeManager = nodeManager;
        this.masterNodeId = masterNodeId;

    }

    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(HTMLResponseService.class.getSimpleName());


    @Override
    public void run() {
        String line;
        BufferedReader fromClient;
        DataOutputStream toClient;

        try {
            String response = "";
            fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
//            toClient = new DataOutputStream(client.getOutputStream()); // TO Client
            PrintStream ps = new PrintStream(client.getOutputStream(), true);

            //Saves all lines from request
            ArrayList <String> requestArray = new ArrayList<String>();
            line = fromClient.readLine();
            requestArray.add(line);

            while (!line.isEmpty()) {     // repeat as long as connection exists
                line = fromClient.readLine();              // Read Request
                requestArray.add(line);
            }

            // Parse received HTTP request
            Map<String,String> parsedRequestHeader = parseHTTPHeader(requestArray);

            // check whether received request is a GET request
            if( parsedRequestHeader.get("httpOperation").equals("GET")){
                // Logging user agent
                LOGGER.info( client.getRemoteSocketAddress() +" "+parsedRequestHeader.get("httpOperation")+ " " + parsedRequestHeader.get("url") + " "+parsedRequestHeader.get("User-Agent"));

                // decode REST values from URL
                Map<String,String> urlParams = decodeURL(parsedRequestHeader.get("url"));


                if (urlParams != null) {
                    // perform REST actions and send json response
                    response = handleRestOperation(urlParams);
                    // send html header
                    ps.println("HTTP/1.1 200 OK");
                    ps.println("Content-Type: application/json");
                    ps.println("\r\n");
                    ps.println(response);
                    ps.flush();
                } else {
                    // if no parameters are provided in URL send default webpage
                    response = generateDefaultPage();
                    // send html header
                    ps.println("HTTP/1.1 200 OK");
                    ps.println("Content-Type: text/html");
                    ps.println("\r\n");
                    ps.println(response);
                    ps.flush();
                }
            }
            
            fromClient.close();
//            toClient.close();
            client.close(); // End
            //LOGGER.info("Thread ended: " + this);
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());
        }
    }

    /**
     * constructs and sends a static html page with information about all known hosts to the client.
     */
    private String generateDefaultPage(){
        try {
            // get all nodes from nodemanager and sort by nodeID
            ArrayList<NodeEntity> allNodes = nodeManager.getAllNodes();
            allNodes.sort((n1, n2) -> n1.getNodeID().compareTo(n2.getNodeID()));

            // set default message when no nodes are available (yet)
            String htmlTable = "<p style='color: red;'>Keine Netzteilnehmer bekannt</p>";
            if (!allNodes.isEmpty()) {
                // prepare html table to list nodes
                String nodeTableItems = "";
//                InetAddress address = InetAddress.getByName("masternode");

                for (NodeEntity node : allNodes) {
                    nodeTableItems +=
                            "<tr>\n"
                                    + "<td><a href=http://localhost:808" + this.masterNodeId + "/?id=" + node.getNodeID() + ">" + node.getNodeID() + "</a></td>\n"
                                    + "<td>" + node.getName() + "</td>\n"
                                    + "<td>" + node.getFullType() + "</td>\n"
                                    + "<td>" + node.getLatestValue().getValue() + "</td>\n"
                                    + "<td>" + node.getLatestValue().getDate() + "</td>\n"
                                    + "<td>" + node.getStatusHTML() + "</td>\n"
                                    + "<td><a href=http://localhost:808\" + this.masterNodeId + \"/?id=" + node.getNodeID() + "&history=1>Historie</a></td>\n"
                                    + "</tr>\n";
                }

                htmlTable =
                        "<table>\n"
                            + "<tr>"
                            + "<th>ID</th>\n"
                            + "<th>Teilnehmer</th>\n"
                            + "<th>Art</th>\n"
                            + "<th>Letzter Wert</th>\n"
                            + "<th>Letze Meldung</th>\n"
                            + "<th>Status</th>\n"
                            + "<th>Historie</th>\n"
                            + "</tr>"
                            + nodeTableItems
                            + "</table>";
            }

            String htmlBody =
                    "<body>\n"
                            + "<style>\n"
                            + "body { font-family: sans-serif; }\n"
                            + "table, th, td { border: 1px solid black; padding: 0.5em; }\n"
                            + "table { border-collapse: collapse; }\n"
                            + "</style>\n"
                            + "<h1>MasterNode #" + this.masterNodeId + "</h1>\n"
                            + "<h3>Nodes:</h3>\n"
                            + htmlTable;

            String html =
                    "<!DOCTYPE html>\n"
                            + "<html>\n"
                            + "<head>\n"
                            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                            + "<meta charset=\"utf-8\">\n"
                            + "</head>\n"
                            + "<body>\n"
                            + htmlBody;

            return html;

        } catch (Exception e) {
            LOGGER.severe("Exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Handles REST Operations and generates json content for a given REST request.
     * @param urlParams
     * @return
     */
    private String handleRestOperation(Map<String, String> urlParams){
        if (urlParams.containsKey("id")) {

            String nodeId = urlParams.get("id");

            try {
                // RPC: turn Node on/off
                if (urlParams.containsKey("setRunning")) {
                    String value = urlParams.get("setRunning");

                    if (value.equals("0")) {
                        this.nodeManager.turnOffNode(nodeId);
                    } else {
                        this.nodeManager.turnOnNode(nodeId);
                    }
                }

                // RPC: set minimum value for Node
                if (urlParams.containsKey("setMinValue")) {
                    String value = urlParams.get("setMinValue");
                    Integer newMinValue = Integer.valueOf(value);
                    this.nodeManager.setNodeMinValue(nodeId, newMinValue);
                }

                // RPC: set maximum value for Node
                if (urlParams.containsKey("setMaxValue")) {
                    String value = urlParams.get("setMaxValue");
                    Integer newMaxValue = Integer.valueOf(value);
                    this.nodeManager.setNodeMaxValue(nodeId, newMaxValue);
                }

                // create JSON response
                JSONObject response = new JSONObject();
                NodeData nodeData = nodeManager.getLatestNodeData(nodeId);
                String nodeStatus = nodeManager.getNodeStatus(nodeId);
                response.put("nodeID", nodeId);
                response.put("date", nodeData.getDate());
                response.put("currentValue", nodeData.getValue());
                response.put("status", nodeStatus);

                if (urlParams.containsKey("history") && urlParams.get("history").equals("1")) {
                    ArrayList<NodeData> nodeHistory = this.nodeManager.getHistory(nodeId);
                    response.put("history", nodeHistory);
                }

                return response.toString();

            } catch (NodeNotFoundException n) {
                LOGGER.severe(n.getMessage());
                return "No Node found with ID " + nodeId;
            }
        }
        return "No ID specified";
    }

    /**
     * Extract REST parameters from URL.
     * @param url
     */
    private Map<String, String> decodeURL(String url) {


            // "/?id=20&history=1"
            // extract rest parameters from URL
            if (url.contains("?")) {
                int start = url.indexOf("?") + 1;
                url = url.substring(start);
                String[] params = url.split("&");

                // set Map
                HashMap<String, String> restParams = new HashMap<String, String>();

                // parse params and put them as key-value-pair into map
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    restParams.put(keyValue[0], keyValue[1]);
                }
                return restParams;
            }
        return null;
    }

    /**
     * Convert HTTPHeader items into hashmap.
     * @param request
     * @return
     */
    private Map<String, String> parseHTTPHeader(ArrayList<String> request){
        HashMap<String, String> headerItems = new HashMap<String, String>();

        // split up first line of http header
        String httpFirstLine = request.get(0);
        String[] httpOperations = httpFirstLine.split(" ");
        if (httpOperations.length == 3) {
            headerItems.put("httpOperation", httpOperations[0]);
            headerItems.put("url", httpOperations[1]);
            headerItems.put("httpVersion", httpOperations[2]);
        }

        // parse the remaining line of the http header
        for (int i = 1; i < request.size(); i++){
            if (!request.get(i).isEmpty()) {
                String headerItem = request.get(i);
                String[] items = headerItem.split(": ");
                headerItems.put(items[0], items[1]);
            }
        }
        return headerItems;
    }
}

