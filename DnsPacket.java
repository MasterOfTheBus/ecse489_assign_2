import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class DnsPacket {
  InetAddress destServer;
  String name, type;
  ByteBuffer data;
  byte[] id = new byte[2]; // this is going to represent a 16 bit unsigned number

  public static final int A_TYPE = 0x0001;
  public static final int NS_TYPE = 0x0002;
  public static final int CNAME_TYPE = 0x0005;
  public static final int MX_TYPE = 0x00f;

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
    //data.allocate(2 * 6 + 63 + 16 * 2);

    byte[] header=constructHeader();
    byte[] question=constructQuestion();
    
    data = ByteBuffer.allocate(header.length+question.length);
   
    if (data == null) {
	System.out.println("byte buffer is null");
    } else if (header == null) {
	System.out.println("header is null");
    }

    for(int i=0;i<header.length;i++){
    	data.put(header[i]);
    }
    
    for(int i=0;i<question.length;i++){
    	data.put(question[i]);
    }
    
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

  private byte[] constructHeader() {
	  
	  byte[] header= new byte[12];
	  
	  header[0]=id[0];
	  header[1]=id[1];
	  header[2]=0b00000101;
	  header[3]=0x00;
	  header[4]=0x00;
	  header[5]=0x01;
	  header[6]=0x00;
	  header[7]=0x00;
	  header[8]=0x00;
	  header[9]=0x00;
	  header[10]=0x00;
	  header[11]=0x00;
	  
	  
	  
	  
	  
	  
	  return header;
	  
//	  ByteBuffer hBuf = ByteBuffer.allocate(12);
//	 hBuf.put(id[0]);
//	 hBuf.put(id[1]);
//	 
//	 int QR=0;
//	 int OPCODE=0000;
//	 int AA=1;
//	 int TC=0;
//	 int RD=1;
//	 int RA=0;
//	 int Z=000;
//	 int RCODE=0000;
//	 int QDCOUNT=0000000000000001;
//	 int ANCOUNT=0000000000000000;
//	 int NSCOUNT=0000000000000000;
//	 int ARCOUNT=0000000000000000;
//	 byte ln21= Byte.parseByte(""+QR+OPCODE+AA+TC+RD,2);
//	 byte ln22= Byte.parseByte(""+RA+Z+RCODE,2);
//	 byte ln31= Byte.parseByte("00000000",2);
//	 byte ln32= Byte.parseByte("00000001",2);
//	 byte ln41= Byte.parseByte("00000000",2);
//	 byte ln42= Byte.parseByte("00000000",2);
//	 byte ln51= Byte.parseByte("00000000",2);
//	 byte ln52= Byte.parseByte("00000000",2);
//	 byte ln61= Byte.parseByte("00000000",2);
//	 byte ln62= Byte.parseByte("00000000",2);
//	 hBuf.put(ln21);
//	 hBuf.put(ln22);
//	 hBuf.put(ln31);
//	 hBuf.put(ln32);
//	 hBuf.put(ln41);
//	 hBuf.put(ln42);
//	 hBuf.put(ln51);
//	 hBuf.put(ln52);
//	 hBuf.put(ln61);
//	 hBuf.put(ln62);
	 
  }

  public byte[] constructQuestion() {
		
	  
	 //QNAME
	    String[] labels=name.split("\\.");
	    int lengthCounter=0;
	    int numOfLabels= labels.length;
	    for(int i=0;i<numOfLabels;i++){
	    	lengthCounter+= labels[i].length();
	    }
	    //Labels+chars+2 from QType+2 from QClass
	    byte[] question = new byte[lengthCounter+numOfLabels+5];
	    int counter=0;
	    for(int i=0;i<numOfLabels;i++){   	
	    	char[] chars=labels[i].toCharArray();
	    	int numOfChars=chars.length;
	    	question[counter]=(byte)numOfChars;
	    	counter++;
	    	for(int j=0;j<numOfChars;j++){
	    	
	    	question[counter]=(byte)chars[j];
	    	counter++;
	    	}

	    }
	    question[counter]=(byte)0;
	    counter++;
	    //QTYPE
	    if(type.equals("A")){
	    	question[counter]=0x00;
	    	counter++;
	    	question[counter]=0x01;
	    	counter++;
	    	
	    }
	    else if(type.equals("MX")){
	    	question[counter]=0x00;
	    	counter++;
	    	question[counter]=0x0f;
	    	counter++;
	    }else if(type.equals("NS")){
	    	question[counter]=0x00;
	    	counter++;
	    	question[counter]=0x02;
	    	counter++;
	    }else{
	    	System.out.println("ERROR:Entered type "+ type +" is not determinable");
	    	
	    }

	    //QCLASS
	    question[counter]=0x00;
    	counter++;
    	question[counter]=0x01;
    	counter++;
	    return question;
	 }

  public static String evaluateRCode(int rcode, boolean auth) {
    switch (rcode) {
      case 0:
        return "";
      case 1:
        return ("ERROR\tResponse from server returned: Format Error");
      case 2:
        return ("ERROR\tResponse from server returned: Server Failure");
      case 3:
        return ((auth) ? ("NOTFOUND\tDomain does not exist") : "");
      case 4:
        return ("ERROR\tResponse from server returned: Not implemented. Server does not support requested query");
      case 5:
        return ("ERROR\tResponse form server returned: Requested operation refused");
      default:
        return ("WARNING\tUnknown return code from server");
    }
  }

  private static int checkCompression(ByteBuffer data) {
    int position = -1;
    byte fromBuf = data.get();
    if ((fromBuf & 0xc0) == (0xc0)) {
      byte[] two_bytes = {(new Integer(fromBuf & 0x3f)).byteValue(), data.get()};
      position = data.position();
      int newPos = convertId(two_bytes);
      data.position(newPos);
    } else {
      data.position(data.position() - 1);
    }
    return position;
  }

  public static String parseName(ByteBuffer data) {
    int position = -1;

    String domain = "";
    position = checkCompression(data);
    int label_length = (int)data.get();

    while (label_length != 0) {
      for (int i = 0; i < label_length; i++) {
        domain += ((char)data.get());
      }
      domain += ".";

      int temp = checkCompression(data);
      if (temp != -1) position = temp;

      label_length = (int)data.get();

    }

    if (position != -1) data.position(position);

    return domain.substring(0, domain.length() - 1);
  }

  public static String[] parseRData(ByteBuffer data, int length, int type) {
    String ret[];
    if (type == A_TYPE) {
      ret = new String[]{""};
      for (int i = 0; i < length; i++) {
        int octet = data.get();
	if (octet < 0) octet += 256;
	ret[0] += octet + ".";
      }
      ret[0] = ret[0].substring(0, ret[0].length() - 1);
    } else if (type == NS_TYPE) {
      ret = new String[]{parseName(data)};
    } else if (type == CNAME_TYPE) {
        ret = new String[]{parseName(data)};
    } else if (type == MX_TYPE) {
      ret = new String[]{"", ""};
      byte[] two_bytes = new byte[2];
      data.get(two_bytes);
      ret[1] = (new Integer(convertId(two_bytes))).toString();

      ret[0] = parseName(data);
    } else {
      return (new String[]{""});
    }

    return ret;
  }

  public static String typeToString(int type) {
    switch (type) {
      case A_TYPE:
        return "IP";
      case CNAME_TYPE:
        return "CNAME";
      case MX_TYPE:
        return "MX";
      case NS_TYPE:
        return "NS";
      default:
        return "";
    }
  }
}
