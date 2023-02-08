package ExternalClient;

import ExternalClient.thrift.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class NodeManager {

  private ArrayList<NodeEntity> nodes;
  private Integer nodeRPCPort = 5002;
  private Integer healthCheckerStatusCode = -2;

  /* Constructor */
  public NodeManager(Integer masterNodeId, Integer nodeRPCPort) {

    this.nodes = new ArrayList<NodeEntity>();
    this.healthCheckerStatusCode = -1;
    this.nodeRPCPort = nodeRPCPort;
  }

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger(NodeManager.class.getSimpleName());

  /**
   * Checks whether a node with a given id exists or not, returns -1 if NodeEntity is not found.
   *
   * @param nodeID
   * @return
   */
  private Integer checkExistingNode(String nodeID) {
    for (int i = 0; i < this.nodes.size(); i++) {
      if (this.nodes.get(i).getNodeID().equals(nodeID)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Adds new node values.
   * If a node is not yet known, a new node entity will be created.
   *
   * @param msg JSON data
   */
  public synchronized void addNewValue(JSONObject msg) {
    try {
      // Extract data from JSON
      String name = msg.getString("name");
      String id = msg.getString("id");
      String dateString = msg.getString("date");
      Date packetDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(dateString);
      Character type = msg.getString("type").toCharArray()[0];
      Integer currentValue = msg.getInt("currentValue");
      String status = msg.getString("status");

      // Check if node is known already. If not a new NodeEntity is created
      Integer index = this.checkExistingNode(id);

      // If node is not known, create new NodeEntity
      if (index.equals(-1)) {
        NodeEntity newNode = new NodeEntity(id, name, type, currentValue, status, packetDate);
        this.nodes.add(newNode);
        LOGGER.info("New NodeEntity created. "+ newNode.toString());
      } else {
        NodeEntity node = this.nodes.get(index);
        // If value belongs to a known node, store it
        node.addNewValue(currentValue, packetDate);

        // set node's reported status
        node.setStatus(status);
      }

    } catch (JSONException e) {
      LOGGER.severe("Error when parsing JSON: " + e.getMessage());
    } catch (ParseException p) {
      LOGGER.severe("Error when parsing date string: " + p.getMessage());
    }
  }



  public void importNodeData(JSONObject nodeData) {
    /**
     *         json.put("nodeID", this.nodeID);
     *         json.put("type", this.type);
     *         json.put("name", this.name);
     *         json.put("currentValue", this.currentValue);
     *         json.put("status", this.status);
     *         json.put("history", this.nodeData);
     */
    try {
      String nodeID = nodeData.getString("nodeID");
      Character type = nodeData.getString("type").charAt(0);
      String name = nodeData.getString("name");
      Integer currentValue = nodeData.getInt("currentValue");
      String status = nodeData.getString("status");
      JSONArray history = nodeData.getJSONArray("history");

      ArrayList<NodeData> nodeHistory = new ArrayList<>();
      for (int i = 0; i < history.length(); i++) {
        try {
          JSONObject historyEntry = history.getJSONObject(i);
          Integer value = historyEntry.getInt("value");
          Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(historyEntry.getString("date"));
          NodeData data = new NodeData(value, date);
          nodeHistory.add(i, data);
        }catch(ParseException e){
          LOGGER.severe("Error when parsing date string: " + e.getMessage());
        }
      }


      NodeEntity nodeEntity = new NodeEntity(nodeID, name, type, currentValue, status, nodeHistory);
      this.nodes.add(nodeEntity);
    } catch (JSONException e) {
      LOGGER.severe("JSONEXCEPTION: " + e.getMessage());
      LOGGER.severe("JSONEXCEPTION: " + nodeData);
    }
  }

  // Returns the complete History of data of a given node
  public ArrayList<NodeData> getHistory(String nodeID) throws NodeNotFoundException{
    Integer index = this.checkExistingNode(nodeID);
    if(!index.equals(-1)){
      return this.nodes.get(index).getAllData();
    }else throw new NodeNotFoundException("Requested Node with id "+ nodeID + " was not found.");
  }

  // Returns the current value of a given node
  public Integer getCurrentValue(String nodeID) throws NodeNotFoundException{
    Integer index = this.checkExistingNode(nodeID);
    if(!index.equals(-1)){
      return this.nodes.get(index).getCurrentValue();
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeID + " was not found.");
  }

  // Returns the current status of a given node
  public String getNodeStatus(String nodeID) throws NodeNotFoundException{
    Integer index = this.checkExistingNode(nodeID);
    if(!index.equals(-1)){
      return this.nodes.get(index).getStatus();
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeID + " was not found.");
  }

  // Returns a list of ID's of all registered nodes
  public ArrayList<String> getAllNodesID(){
    ArrayList<String> idList = new ArrayList<>();
    for (NodeEntity node: this.nodes) {
      idList.add(node.getNodeID());
    }
    return idList;
  }

  // Returns all node objects managed by this nodemanager
  public ArrayList<NodeEntity> getAllNodes() {
    return this.nodes;
  }

  // Returns a single node, identified by its ID
  public NodeEntity getSingleNode(String nodeId) {
    for (NodeEntity node: this.nodes
    ) {
      if (node.getNodeID().equals(nodeId)){
        return node;
      }
    }
    return null;
  }

  public NodeData getLatestNodeData(String nodeId) throws NodeNotFoundException {
    Integer index = this.checkExistingNode(nodeId);
    if(!index.equals(-1)){
      return this.nodes.get(index).getLatestValue();
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeId + " was not found.");
  }

  public Integer getHealthCheckerStatusCode() {
    return healthCheckerStatusCode;
  }

  public void setHealthCheckerStatusCode(Integer healthCheckerStatusCode) {
    this.healthCheckerStatusCode = healthCheckerStatusCode;
  }

  public String getStatusHTML() {
    // Return status as formatted HTML-String
    Integer statusCode = this.getHealthCheckerStatusCode();
    switch (statusCode) {
      case -2: {
        return "<div style='color: gray;'>waiting for status ... </div>";
      }
      case -1: {
        return "<div style='color: red;'>unhealthy</div>";
      }
      case 1: {
        return "<div style='color: green;'>healthy</div>";
      }
      default: {
        return "<div>" + statusCode + "</div>";
      }
    }
  }

  /** --- RPC Function calls --- **/

  public void turnOnNode(String nodeId) throws NodeNotFoundException {
    Integer index = this.checkExistingNode(nodeId);
    if(!index.equals(-1)) {
      String name = this.getSingleNode(nodeId).getName();
      // Create RPC client
      NodeConfigClient client = new NodeConfigClient(name, this.nodeRPCPort);
      client.turnNodeOn();
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeId + " was not found.");
  }

  public void turnOffNode(String nodeId) throws NodeNotFoundException {
    Integer index = this.checkExistingNode(nodeId);
    if(!index.equals(-1)) {
      String name = this.getSingleNode(nodeId).getName();
      // Create RPC client
      NodeConfigClient client = new NodeConfigClient(name, this.nodeRPCPort);
      client.turnNodeOff();
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeId + " was not found.");
  }

  public void setNodeMinValue (String nodeId, Integer value) throws NodeNotFoundException {
    Integer index = this.checkExistingNode(nodeId);
    if(!index.equals(-1)) {
      String name = this.getSingleNode(nodeId).getName();
      // Create RPC client
      NodeConfigClient client = new NodeConfigClient(name, this.nodeRPCPort);
      client.setNodeMinValue(value);
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeId + " was not found.");
  }

  public void setNodeMaxValue (String nodeId, Integer value) throws NodeNotFoundException {
    Integer index = this.checkExistingNode(nodeId);
    if(!index.equals(-1)) {
      String name = this.getSingleNode(nodeId).getName();
      // Create RPC client
      NodeConfigClient client = new NodeConfigClient(name, this.nodeRPCPort);
      client.setNodeMaxValue(value);
    } else throw new NodeNotFoundException("Requested Node with id "+ nodeId + " was not found.");
  }
}
