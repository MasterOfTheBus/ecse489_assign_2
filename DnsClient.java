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
    System.out.println("destination: " + packet.destServer);

    sendRequest(packet, timeout, max_retries, port);
    //receiveResponse();
    
    return;
  }

  static void sendRequest(DnsPacket packet, int timeout, int max_retries, int port) {
    try {
      DatagramSocket socket = new DatagramSocket();
    } catch (SocketException se) {
	System.out.println("ERROR\tCould not create socket");
	return;
    }

    System.out.println("DnsClient sending request for " + packet.name);
    System.out.println("Server: " + packet.destServer.toString());
    System.out.println("Request type: " + packet.type);

   /* 
    String sentence = "hello, world!";
    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    sendData = sentence.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHere, 9876);
    socket.send(sendPacket);
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    socket.receive(receivePacket);
    String modSent = new String(receivePacket.getData());
    System.out.println("FROM SERVER: " + modSent);
    socket.close();*/
  }

  static void receiveResponse() {

  }
}
