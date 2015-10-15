import java.lang.*;
import java.net.*;
import java.io.*;

public class DnsPacket {
  InetAddress destServer;
  String name, type;
  public byte[] data;

  public DnsPacket(String server, String name, String type) {
    // store the value of the destination server as an inet address
    int periodIndex = 0;
    int start = 0;
    byte[] str2byte = new byte[4];
    for (int i = 0; i < str2byte.length; i++) {
      int end = server.indexOf('.', periodIndex);
      if (end == -1) end = server.length();
      int fromAddr = Integer.parseInt(server.substring(start, end));
      str2byte[i] = (new Integer(fromAddr)).byteValue();

      start = end + 1;
      periodIndex = end + 1;
    }

    try {
      destServer = InetAddress.getByAddress(str2byte);
    } catch (UnknownHostException e) {
      System.out.println("ERROR\tUnknown Host: " + server);
      return;
    }

    this.name = name;
    this.type = type;
  }

  public byte[] constructHeader() {
    byte[] header = new byte[0];

    return header;
  }

  public byte[] constructQuestion() {
    byte[] question = new byte[0];

    return question;
  }

}
