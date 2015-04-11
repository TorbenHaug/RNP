package utils.adt;

import java.rmi.server.UID;

public class NetworkToken {
	
	private String message;
	private UID ID;
	private String IP;
	
	
	public NetworkToken(String message, UID ID, String IP) {
		this.message = message;
		this.ID = ID;
		this.IP = IP;
	}
	
	
	public String getMessage() {
		return message;
	}
	
	
	public UID getID() {
		return ID;
	}
	
	
	public String getIP() {
		return IP;
	}
	
	
}
