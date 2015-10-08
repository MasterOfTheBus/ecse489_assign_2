import java.io.*;

public class DnsClient {

  static final String usage_str = "Usage is: java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name";

  int timeout = 5; // seconds
  int max_retries = 3;
  int port = 53;
  String type = "A"; // MX, NS, or A

  public static void main(String[] args) {
    String server = "";
    String name = "";

    if (args.length <= 0) {
      System.out.println(usage_str);
      return;
    }

    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("@")) {
	server = args[i].substring(1);
      } else if (i == args.length - 1) {
	name = args[i];
      }
    }

    DnsPacket packet = new DnsPacket();

    sendRequest();
    receiveResponse();

    return;
  }

  static void sendRequest() {

  }

  static void receiveResponse() {

  }
}
