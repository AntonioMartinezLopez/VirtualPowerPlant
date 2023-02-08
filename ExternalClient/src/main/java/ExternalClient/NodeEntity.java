package ExternalClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NodeEntity {

  private String nodeID;
  private String name;
  private Character type;
  private Integer currentValue;
  private String status;
  private ArrayList<NodeData> nodeData;

  public String getFullType() {
    switch (this.type) {
      case 'c': { return "Consumer";}
      case 'C': { return "Consumer";}
      case 'p': { return "Producer";}
      case 'P': { return "Producer";}
    }
    return null;
  }

  public NodeEntity(String nodeID, String name, Character type, Integer currentValue, String status, Date date) {
    this.nodeID = nodeID;
    this.name = name;
    this.type = type;
    this.currentValue = currentValue;
    this.status = status;


    /* Contains all received information from corresponding node */
    this.nodeData = new ArrayList<NodeData>();
    this.nodeData.add(new NodeData(this.currentValue, date));
  }

  public NodeEntity(String nodeID, String name, Character type, Integer currentValue, String status, ArrayList<NodeData> history) {
    this.nodeID = nodeID;
    this.name = name.toLowerCase(Locale.ROOT);
    this.type = type;
    this.currentValue = currentValue;
    this.status = status;

    /* import history */
    this.nodeData = history;
  }

  public void addNewValue(Integer newValue, Date date) {
    this.nodeData.add(new NodeData(newValue, date));
    this.currentValue = newValue;
  }

  public String getName() {
    return name;
  }

  public NodeData getLatestValue() {
    return this.nodeData.get(this.nodeData.size() -1 );
  }

  public Integer getCurrentValue(){
    return this.currentValue;
  }

  public ArrayList<NodeData> getAllData(){
    return this.nodeData;
  }

  public String getNodeID() {
    return nodeID;
  }

  @Override
  public String toString() {
    return "Node: " + this.name +"; Node-ID: " + this.nodeID + "; Type: " + this.type + ";";
  }

  public String getStatus() {
    return status;
  }

  public String getStatusHTML() {
    if (this.status.equals("online")) {
      return "<div style='color: green;'>" + status + "</div>";
    } else if (this.status.equals("offline")) {
      return "<div style='color: red;'>" + status + "</div>";
    } else if (this.status.equals("unhealthy")) {
      return "<div style='color: orange;'>" + status + "</div>";
    } else {
      return status;
    }

  }

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Return a JSON representation of the Node entity and its history
   * @return
   */
  public JSONObject getJSON() {
    JSONObject json = new JSONObject();
    json.put("nodeID", this.nodeID);
    json.put("type", this.type);
    json.put("name", this.name);
    json.put("currentValue", this.currentValue);
    json.put("status", this.status);
    json.put("history", this.nodeData);

    return json;
  }
}
