package ExternalClient;

import org.json.JSONObject;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HTMLResponseService extends Thread {

  private Socket client;
  private MasterNodeManager masterNodeManager;
  private String[] masterNodeIds;

  public HTMLResponseService(Socket client, MasterNodeManager masterNodeManager, String[] masterNodeIds) {
    this.client = client;
    this.masterNodeManager = masterNodeManager;
    this.masterNodeIds = masterNodeIds;
  }

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(HTMLResponseService.class.getSimpleName());


  @Override
  public void run() {
    String line;
    BufferedReader fromClient;
    DataOutputStream toClient;

    try {
      fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
//            toClient = new DataOutputStream(client.getOutputStream()); // TO Client
      PrintStream ps = new PrintStream(client.getOutputStream(), true);

      //Saves all lines from request
      ArrayList<String> requestArray = new ArrayList<String>();
      line = fromClient.readLine();
      requestArray.add(line);

      while (!line.isEmpty()) {     // repeat as long as connection exists
        line = fromClient.readLine();              // Read Request
        requestArray.add(line);
      }

      // Parse received HTTP request
      Map<String, String> parsedRequestHeader = parseHTTPHeader(requestArray);

      // check whether received request is a GET request
      if (parsedRequestHeader.get("httpOperation").equals("GET")) {
        // Logging user agent
        LOGGER.info(client.getRemoteSocketAddress() + " " + parsedRequestHeader.get("httpOperation") + " " + parsedRequestHeader.get("url") + " " + parsedRequestHeader.get("User-Agent"));

        // decode REST values from URL
        Map<String, String> urlParams = decodeURL(parsedRequestHeader.get("url"));


        if (urlParams != null) {
          String[] response;
          // perform REST actions and send json response
          response = handleRestOperation(urlParams);
          // send html header
          ps.println("HTTP/1.1 200 OK");
          if (response[0].equals("json")) {
            ps.println("Content-Type: application/json");
          } else {
            ps.println("Content-Type: text/html");
          }
          ps.println("\r\n");
          ps.println(response[1]);
          ps.flush();

        } else {
          String response;
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
  private String generateDefaultPage() {
    try {
      HashMap<Integer, NodeManager> allNodeManagers = this.masterNodeManager.getAllNodemanagers();
      String nodeManagerItems = "";
      if (!allNodeManagers.isEmpty()) {
        // prepare html table to list nodes



        for (Integer masterNodeId : allNodeManagers.keySet()) {
          String nodeTableItems = "";
          NodeManager nodeManager = allNodeManagers.get(masterNodeId);
          nodeManagerItems +=
                  "<h2><a href=http://localhost:808" + masterNodeId + "/>MasterNode #" + masterNodeId + "<a/> Nodes</h2>\n"
                          + "<table>\n"
                          + "<tr>"
                          + "<th>ID</th>\n"
                          + "<th>Name</th>\n"
                          + "<th>Type</th>\n"
                          + "<th>Last value</th>\n"
                          + "<th>Last seen</th>\n"
                          + "<th>Status</th>\n"
                          + "<th>History</th>\n"
                          + "<th>Actions</th>\n"
                          + "</tr>";

          if (!nodeManager.getAllNodes().isEmpty()) {
            for (NodeEntity node : nodeManager.getAllNodes()) {
              String nodeStatus = node.getStatus();
              nodeTableItems +=
                      "<tr>\n"
                              + "<td>" + node.getNodeID() + "</td>\n"
                              + "<td>" + node.getName() + "</td>\n"
                              + "<td>" + node.getFullType() + "</td>\n"
                              + "<td>" + node.getLatestValue().getValue() + "</td>\n"
                              + "<td>" + node.getLatestValue().getDate() + "</td>\n"
                              + "<td>" + node.getStatusHTML() + "</td>\n"
                              + "<td><a href=http://localhost:808" + masterNodeId + "/?id=" + node.getNodeID() + "&history=1>History</a></td>\n"
                              + "<td><a href=http://localhost:8080/?masternodeid=" + masterNodeId + "&nodeid=" + node.getNodeID() + "&setRunning=" + (nodeStatus.equals("online") ? "0" : "1")+ "><button>"+ (nodeStatus.equals("online") ? "Turn off" : "Turn on") +"</button></a>\n"
                              + "<a href=http://localhost:8080/?masternodeid=" + masterNodeId + "&nodeid=" + node.getNodeID() + "&setMinValue=999><button>SetMinValue</button></a>\n"
                              + "<a href=http://localhost:8080/?masternodeid=" + masterNodeId + "&nodeid=" + node.getNodeID() + "&setMaxValue=999><button>SetMaxValue</button></a></td>\n"
                              + "</tr>\n";
            }
            nodeManagerItems += nodeTableItems;
          }
          nodeManagerItems += "</table>";
        }
      }

      String masterNodeItems = "";
        if (this.masterNodeIds.length != 0) {
          // prepare html table to list MasterNodes
          masterNodeItems +=
                  "<table>\n"
                          + "<tr>"
                          + "<th>ID</th>\n"
                          + "<th>Hostname</th>\n"
                          + "<th>Status</th>\n"
                          + "<th>Actions</th>\n"
                          + "</tr>";

          for (String masterNodeId : masterNodeIds) {
            Integer status = this.masterNodeManager.getNodeManager(Integer.valueOf(masterNodeId)).getHealthCheckerStatusCode();
            masterNodeItems +=
                                    "<tr>\n"
                                    + "<td>" + masterNodeId + "</td>\n"
                                    + "<td>masternode-" + masterNodeId + "</td>\n"
                                    + "<td>" + this.masterNodeManager.getNodeManager(Integer.valueOf(masterNodeId)).getStatusHTML() + "</td>\n"
                                    + "<td><a href=http://localhost:8080/?masternodeid=" + masterNodeId + "&collectData=1000><button " + (status.equals(1) ? "" : "disabled")
                                            + ">collect data</button></a></td>\n"
                                    + "</tr>";
          }


          masterNodeItems += "</table>";
        }

        String htmlBody =
                "<body>\n"
                        + "<style>\n"
                        + "body { font-family: sans-serif; }\n"
                        + "table, th, td { border: 1px solid black; padding: 0.5em; }\n"
                        + "table { border-collapse: collapse; }\n"
                        + "</style>\n"
                        + "<h1>External Client</h1>\n"
                        + "<a href=http://localhost:8080/><button>Reload page</button></a>"
                        + "<p>List of known MasterNodes:</p>\n"
                        + masterNodeItems
                        + nodeManagerItems;

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
  private String[] handleRestOperation(Map<String, String> urlParams){
    if (urlParams.containsKey("collectData") && urlParams.containsKey("masternodeid")) {

      int masterNodeId = Integer.parseInt(urlParams.get("masternodeid"));
      int maxBlockSize = Integer.parseInt(urlParams.get("collectData"));

      long startTime = System.currentTimeMillis();
      this.masterNodeManager.collectReplicationData(masterNodeId, maxBlockSize);
      long endTime = System.currentTimeMillis();
      long time = endTime - startTime;
      LOGGER.info("Required time for fetching complete history with blockSize " + maxBlockSize + ": " + time + " ms");

      return new String[]{"text", this.generateDefaultPage()};
    }

//    if (urlParams.containsKey("id")) {
//
//
//
//      try {
//        // create JSON response
//        JSONObject response = new JSONObject();
//        NodeData nodeData = nodeManager.getLatestNodeData(nodeId);
//        String nodeStatus = nodeManager.getNodeStatus(nodeId);
//        response.put("nodeID", nodeId);
//        response.put("date", nodeData.getDate());
//        response.put("currentValue", nodeData.getValue());
//        response.put("status", nodeStatus);
//
//        if (urlParams.containsKey("history") && urlParams.get("history").equals("1")) {
//          ArrayList<NodeData> nodeHistory = this.nodeManager.getHistory(nodeId);
//          response.put("history", nodeHistory);
//        }
//
//        return new String[]{"json",response.toString()};
//
//      } catch (NodeNotFoundException n) {
//        LOGGER.severe(n.getMessage());
//        return new String[]{"json","No Node found with ID " + nodeId};
//      }
//    }

    if (urlParams.containsKey("masternodeid") &&
            urlParams.containsKey("nodeid")) {
      try {
        String nodeId = urlParams.get("nodeid");
        String masterNodeId = urlParams.get("masternodeid");
        NodeManager nodeManager = this.masterNodeManager.getNodeManager(Integer.valueOf(masterNodeId));

        // RPC: turn Node on/off
        if (urlParams.containsKey("setRunning")) {
          String value = urlParams.get("setRunning");

          if (value.equals("0")) {
            nodeManager.turnOffNode(nodeId);
            return new String[]{"text", "<p>Node has been <b>deactivated</b>.</p><a href=http://localhost:8080/>Return to dashboard</a>"};
          } else {
            nodeManager.turnOnNode(nodeId);
            return new String[]{"text", "<p>Node has been <b>activated</b>.</p><a href=http://localhost:8080/>Return to dashboard</a>"};
          }
        }

        // RPC: set minimum value for Node
        if (urlParams.containsKey("setMinValue")) {
          String value = urlParams.get("setMinValue");
          Integer newMinValue = Integer.valueOf(value);
          nodeManager.setNodeMinValue(nodeId, newMinValue);
          return new String[]{"text", "<p>New <b>MinValue</b> has been set.</p><a href=http://localhost:8080/>Return to dashboard</a>"};
        }

        // RPC: set maximum value for Node
        if (urlParams.containsKey("setMaxValue")) {
          String value = urlParams.get("setMaxValue");
          Integer newMaxValue = Integer.valueOf(value);
          nodeManager.setNodeMaxValue(nodeId, newMaxValue);
          return new String[]{"text", "<p>New <b>MaxValue</b> has been set.</p><a href=http://localhost:8080/>Return to dashboard</a>"};
        }
      } catch (NodeNotFoundException e) {
        return new String[]{"text","Node not found"};
      }


    }

    return new String[]{"text","No valid REST operation found"};
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

      // Hashmap containing rest parameters
      HashMap<String, String> restParams = new HashMap<String, String>();


      if (url.contains("&")){
        // if multiple parameters are given
        String[] params = url.split("&");

        // parse params and put them as key-value-pair into map
        for (String param : params) {
          String[] keyValue = param.split("=");
          restParams.put(keyValue[0], keyValue[1]);
        }
      } else {
        // if single parameter is given
        String param = url.substring(url.indexOf("?") + 1);
        String[] keyValue = param.split("=");
        if (keyValue.length == 1) {
          LOGGER.warning("Single REST parameter with no value has been given");
          return null;
        }
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