import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class DnsPacket {
  InetAddress destServer;
  String name, type;
  ByteBuffer data;
  byte[] id = new byte[2]; // this is going to represent a 16 bit unsigned number

  public DnsPacket(String server, String name, String type) {
    // store the value of the destination server as an inet address
    int periodIndex = 0;
    int start = 0;
    byte[] str2byte = new byte[4];
    for (int i = 0; i < str2byte.length; i++) {
      int end = server.indexOf('.', periodIndex);
      if (end == -1) end = server.length();
      int fromAddr = Integer.parseInt(server.substring(start, end));
      if (fromAddr < 0 || fromAddr > 255) {
	System.out.printf("ERROR\tInvalid IP address: " + server);
      }
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

    // the data has been passed correctly, assign a random id number
    Random rand = new Random();
    rand.nextBytes(id);

    // create the bytebuffer that will store all the data
    // the amount of data to allocate is 2 bytes * 6 (header) + 63 + 16 * 2 (question)
    data.allocate(2 * 6 + 63 + 16 * 2);

    constructHeader();
    
  }

  public static int convertId(byte[] id) {
    int ret = id[0];
    if (ret < 0) ret += 256;
    ret = ret << 8;
    int temp = id[1];
    if (id[1] < 0) temp += 256;
    ret += temp;
    return ret;
  }

  private void constructHeader() {

  }

  public byte[] constructQuestion() {
    byte[] question = new byte[0];

    return question;
  }

}
