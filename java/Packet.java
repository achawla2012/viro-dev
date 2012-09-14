import java.io.*;
import java.net.*;

public class Packet
{
	private String opCode;
	private String src;
	private String dest;
	private String payload;
	private String fwd;
	private String ttl;
	
	public Packet() {

	}
	
	public String getOpcode() {
		return opCode;
	}
	
	public void setOpcode(String value) { 
		opCode = value;
	}
	
	public String getSource() {
		return src;
	}
	
	public void setSource(String value) {
		src = value;
	}
	
	public String getDest() {
		return dest;
	}
	
	public void setDest(String value) {
		dest = value;
	}
	
	public String getFwd() {
		return fwd;
	}
	
	public void setFwd(String value) {
		fwd = value;
	}
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String value) {
		payload = value;
	}
	
	public String getTTL() {
		return ttl;
	}
	
	public void setTTL(String value) {
		ttl = value;
	}	
		
} // End of Packet class

// End of File
