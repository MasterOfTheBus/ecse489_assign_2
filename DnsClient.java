import java.io.*;
import java.net.*;
import java.nio.*;

public class DnsClient {

  static final String usage_str = "Usage is: java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name";
  static final String unexpected_str = "ERROR\tUnexpected Response: ";

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
	  System.out.println("ERROR\tInvalid timeout value: " + args[i+1]);
	  return;
	}
	if (timeout < 0) {
	  System.out.println("ERROR\tInvalid timeout value: timeout must be a positive integer");
	  return;
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
	  System.out.println("ERROR\tIncorrect retries value: " + args[i+1]);
	  return;
	}
	if (max_retries < 0) {
	  System.out.println("ERROR\tIncorrect retries value: retries must be a positive integer");
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
	  System.out.println("ERROR\tIncorrect port value: " + args[i+1]);
	  return;
	}
	if (port < 0) {
	  System.out.println("ERROR\tIncorrect port value: port must be a positive integer");
	  return;
	}
      } else {
	System.out.println("ERROR\tIncorrect input syntax\n" + usage_str);
	return;
      }
    }

    DnsPacket packet = new DnsPacket(server, name, type);
    if (packet.destServer == null) return;
    byte[] data = sendRequest(packet, timeout, max_retries, port);
   
    if (data.length != 0) parseReceivedData(packet, data);

    return;
  }

  static byte[] sendRequest(DnsPacket packet, int timeout, int max_retries, int port) {
    try {
      DatagramSocket socket = new DatagramSocket();
    
      try {
        socket.setSoTimeout(timeout * 1000);
      } catch (SocketException se) {
        System.out.println("ERROR\tFailed to set socket timeout " + timeout);
        return (new byte[0]);
      }

      System.out.println("\nDnsClient sending request for " + packet.name);
      System.out.println("Server: " + packet.destServer.toString().substring(1));
      System.out.println("Request type: " + packet.type + "\n");

      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
      DatagramPacket sendPacket = new DatagramPacket(packet.data.array(), packet.data.array().length, packet.destServer, port);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
   
      int attempt = 0;
      long millis = System.currentTimeMillis();
      while (attempt < max_retries) {
        try {
          socket.send(sendPacket);
	  socket.receive(receivePacket);
	  if (receivePacket.getData().length == 0) {
	    attempt++;
	    continue;
	  }
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
      millis = System.currentTimeMillis() - millis;
      
      if (attempt == max_retries) {
        System.out.println("ERROR\tMaximum number of retries " + max_retries + " exceeded");
        return (new byte[0]);
      }

      System.out.println("Response received after " + millis / 1000.0 + " seconds (" + attempt + " retries)");

      //String receivedStr = new String(receivePacket.getData());
      //System.out.println("FROM SERVER: " + receivedStr);

      socket.close();

      return (receivePacket.getData());

    } catch (SocketException se) {
	System.out.println("ERROR\tCould not create socket");
	return (new byte[0]);
    }
 }

  static void parseReceivedData(DnsPacket packet, byte[] data) {
    ByteBuffer recvData;
    recvData = ByteBuffer.allocate(data.length);
    recvData.put(data);
    recvData.position(0);

    boolean auth = false;

   //================HEADER===============

   // compare the ids
    byte[] two_bytes = new byte[2];
    recvData.get(two_bytes);
    if (DnsPacket.convertId(packet.id) != DnsPacket.convertId(two_bytes)) {
	System.out.println(unexpected_str + "Query and Response ids do not match");
	return;
    }

    recvData.get(two_bytes);
    // check for the response flag
    if (((int)two_bytes[0] & 0x80) != 0x80) {
	System.out.println(unexpected_str + "Packet received does not contain a DNS response");
    }
    
    // Check the OPCODE
    if (((int)two_bytes[0] & 0x78) != 0x00) {
	System.out.println(unexpected_str + "Response is not for a standard query");
    }

    // Check the auth bit
    auth = (((int)two_bytes[0] & 0x04) == 0x04) ? true : false;

    // Check RA
    if (((int)two_bytes[1] & 0x80) != 0x80) {
	System.out.println("WARNING\tServer does not support recursive queries");
    }

    // Check the RCode
    int rcode = (int)two_bytes[1] & 0x0F;
    String rcodeRet = DnsPacket.evaluateRCode(rcode, auth);
    System.out.println(rcodeRet);
    if (rcodeRet.startsWith("ERROR")) return;

    // the function takes a 2 byte array, might as well use it...
    recvData.get(two_bytes); // bytes 4, 5

    // check the question count
    int qdcount = DnsPacket.convertId(two_bytes);

    recvData.get(two_bytes); // bytes 6, 7
    int ancount = DnsPacket.convertId(two_bytes); 

    recvData.get(two_bytes);
    int nscount = DnsPacket.convertId(two_bytes);

//recvData.position(recvData.position() + 2);
    recvData.get(two_bytes); // bytes 10, 11
    int arcount = DnsPacket.convertId(two_bytes);

    //=============HEADER END====================

    // get through the question section
    String name = DnsPacket.parseName(recvData);

    recvData.position(recvData.position() + 4);

    //=============ANSWER===================
    System.out.println("***Answer Section (" + ancount + ")***");
    if (ancount == 0) System.out.println("NOTFOUND");

    parseAnswerOrAdditional(recvData, ancount, auth);

    //=============ANSWER END=========================

    //===============ADDITIONAL SECTION================
    System.out.println("***Additional Section (" + arcount + ")***");
    if (arcount == 0) System.out.println("NOTFOUND");
 
    parseAnswerOrAdditional(recvData, arcount, auth);
 }

  private static String printAsBinary(byte[] convert) {
    String ret = "";
    for (int i = 0; i < convert.length; i++) {
      ret += Integer.toBinaryString((convert[i] & 0xFF) + 0x100).substring(1);
    }
    return ret;
  }

  private static void repositionBuffer(ByteBuffer buff, int offset) {
    byte two_bytes[] = new byte[2];
    buff.position(buff.position() + offset);
    System.out.println("repositioned to " + buff.position());
    buff.get(two_bytes);
    int rdlength = DnsPacket.convertId(two_bytes);
    System.out.println("reposition +" + rdlength);
    buff.position(buff.position() + rdlength);
  }

  private static void parseAnswerOrAdditional(ByteBuffer recvData, int count, boolean auth) {
    for (int i = 0; i < count; i++) {
      // name
      String name = DnsPacket.parseName(recvData);

      // type
      byte two_bytes[] = new byte[2];
      recvData.get(two_bytes);
      int type = DnsPacket.convertId(two_bytes);
      if (type != DnsPacket.A_TYPE && type != DnsPacket.NS_TYPE && type != DnsPacket.CNAME_TYPE &&
	  type != DnsPacket.MX_TYPE) {
	System.out.println("Unrecognized record type " + type);
	repositionBuffer(recvData, 6);
	continue;
      }

      // class
      recvData.get(two_bytes);
      if (DnsPacket.convertId(two_bytes) != 0x0001) {
	System.out.println(unexpected_str + "Unexpected class value");
	repositionBuffer(recvData, 4);
	continue;
      }

      // TTL
      int TTL = recvData.getInt();
      if (TTL < 0) {
	System.out.println(unexpected_str + "Negative TTL");
      }

      // rdlength
      recvData.get(two_bytes);
      int rdlength = DnsPacket.convertId(two_bytes);

      // RData
      String[] alias = DnsPacket.parseRData(recvData, rdlength, type);
      if (type == DnsPacket.A_TYPE || type == DnsPacket.CNAME_TYPE || type == DnsPacket.NS_TYPE) {
	System.out.println(DnsPacket.typeToString(type) + "\t" + alias[0] + "\t" + TTL + "\t" + ((auth) ? "auth" : "noauth"));
      } else if (type == DnsPacket.MX_TYPE) {
	System.out.println("MX\t" + alias[0] + "\t" + alias[1] + "\t" + TTL + "\t" + ((auth) ? "auth" : "noauth"));	
      } else {
	System.out.println("ERROR\tUnrecognized DNS type");
      }
   }

  }
}
