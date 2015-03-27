package server.adt;

import java.rmi.server.UID;

public class NetworkTocken {
	
	private String message;
	private UID clientID;
	private String clientIP;
	
	
	public NetworkTocken(String message, UID clientID, String clientIP) {
		this.message = message;
		this.clientID = clientID;
		this.clientIP = clientIP;
	}
	
	
	public String getMessage() {
		return message;
	}
	
	
	public UID getClientID() {
		return clientID;
	}
	
	
	public String getClientIP() {
		return clientIP;
	}
	
	
}
