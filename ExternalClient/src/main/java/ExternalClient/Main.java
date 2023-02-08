package ExternalClient;

import java.util.logging.Logger;

public class Main {

  /* Logger */
  private static final Logger LOGGER = Logger.getLogger("MAIN");

  /* Format Logger output */
  static {
    System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT][%4$-7s][%3$-19s] %5$s %n");
  }


  public static void main(String[] args) {

    try {
      // Collect environment variables
      Integer masterNodeRpcPort = Integer.valueOf(System.getenv("MASTERNODERPCPORT"));
      Integer nodeRpcPort = Integer.valueOf(System.getenv("NODERPCPORT"));
//      String masterNodeHostname = System.getenv("MASTERNODEHOSTNAME");
      String[] masterNodeIds = System.getenv("MASTERNODEIDS").split(",");
      Integer healthCheckRate  = Integer.valueOf(System.getenv("HEALTHCHECKRATE"));

      MasterNodeManager masterNodeManager = new MasterNodeManager(masterNodeRpcPort, nodeRpcPort);

      /* Start the Webserver for the client */
      WebServer webServer = new WebServer(masterNodeManager, masterNodeIds);
      webServer.start();

      /* Start the HealthChecker instance */
      MasterNodeHealthChecker healthChecker = new MasterNodeHealthChecker(masterNodeManager, masterNodeIds, healthCheckRate, masterNodeRpcPort);
      healthChecker.start();

    } catch (NumberFormatException ne) {
      LOGGER.severe("Missing environment variables! Please provide all necessary environment variables.\n" + ne.getMessage());
      System.exit(1);
    }
  }
}
