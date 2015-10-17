import java.io.*;
import java.net.*;

public class DnsClient {

  static final String usage_str = "Usage is: java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name";

  public static void main(String[] args) {
    int timeout = 5; // seconds
    int max_retries = 3;
    int port = 53;
    String type = "A"; // MX, NS, or A

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
      } else if (args[i].startsWith("-t")) {
	if (i + 1 >= args.length) {
	  System.out.println(usage_str);
	  return;
	}
	try {
          timeout = Integer.parseInt(args[i+1]);
	  i++;
	} catch (NumberFormatException e) {
	  System.out.println("ERROR\tIncorrect input syntax: Timeout must be a positive integer.");
	}
      } else if (args[i].startsWith("-r")) {
	if (i + 1 >= args.length) {
	  System.out.println(usage_str);
	  return;
	}
	try {
  	  max_retries = Integer.parseInt(args[i+1]);
	  i++;
	} catch (NumberFormatException e) {
	  System.out.println("ERROR\tIncorrect input syntax: Max_retries must be a positive integer.");
	  return;
	}
      } else if (args[i].contains("-mx") || args[i].contains("-ns")) {
	type = (args[i].contains("-mx")) ? "MX" : "NS";
      } else if (args[i].startsWith("-p")) {
	if (i + 1 >= args.length) {
	  System.out.println(usage_str);
	  return;
	}
	try {
  	  port = Integer.parseInt(args[i+1]);
	  i++;
	} catch (NumberFormatException e) {
	  System.out.println("ERROR\tIncorrect input syntax: Port must be a positive integer.");
	  return;
	}
      } else {
	System.out.println("ERROR\tIncorrect input syntax\n" + usage_str);
	return;
      }
    }

    DnsPacket packet = new DnsPacket(server, name, type);

    byte[] data = sendRequest(packet, timeout, max_retries, port);
    parseReceivedData(packet, data);
    
    return;
  }

  static byte[] sendRequest(DnsPacket packet, int timeout, int max_retries, int port) {
    try {
      DatagramSocket socket = new DatagramSocket();
    
      try {
        socket.setSoTimeout(timeout);
      } catch (SocketException se) {
        System.out.println("ERROR\tFailed to set socket timeout " + timeout);
        return (new byte[0]);
      }

      System.out.println("DnsClient sending request for " + packet.name);
      System.out.println("Server: " + packet.destServer.toString().substring(1));
      System.out.println("Request type: " + packet.type);

      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
      sendData = packet.name.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.destServer, port);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
   
      int attempt = 0;
      while (attempt < max_retries) {
        System.out.println("attempt: " + attempt);
        try {
          socket.send(sendPacket);
	  socket.receive(receivePacket);
	  break;
        } catch (IOException ie) {
          if (ie instanceof SocketTimeoutException) {
	    attempt++;
	  } else {
            System.out.println("ERROR\tSocket I/O error");
            return (new byte[0]);
	  }
        }
      }
      
      if (attempt == max_retries) {
        System.out.println("ERROR\tMaximum number of retries " + max_retries + " exceeded");
        return (new byte[0]);
      }

      String receivedStr = new String(receivePacket.getData());
      System.out.println("FROM SERVER: " + receivedStr);

      socket.close();

      return (receivePacket.getData());
    
    } catch (SocketException se) {
	System.out.println("ERROR\tCould not create socket");
	return (new byte[0]);
    }
  }

  static void parseReceivedData(DnsPacket packet, byte[] data) {
    byte A = 0x3;
    System.out.println("A: " + A);
  }

}
