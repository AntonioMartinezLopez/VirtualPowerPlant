package ExternalClient;

import java.util.Date;

public class NodeData {
  private Date date;
  private Integer value;

  public NodeData(Integer value, Date date) {
    this.value = value;
    this.date = date;
  }

  public Integer getValue() {
    return value;
  }

  public Date getDate() {
    return date;
  }

  @Override
  public String toString() {
    return "NodeData{" +
            "date=" + date +
            ", value=" + value +
            '}';
  }
}