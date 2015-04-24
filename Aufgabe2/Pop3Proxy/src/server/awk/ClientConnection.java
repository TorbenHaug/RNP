package server.awk;

import java.rmi.server.UID;
import java.util.Set;
import java.util.UUID;

import client.connectionManager.ClientConnectionManager;
import pop3.proxy.client.StopListener;
import pop3.proxy.configReader.Configs;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ClientConnection{

	private Configs config;
	private final StopListener listener;
	private final UID connectionID;
	private final InputBuffer<NetworkToken> buffer;
	
	public ClientConnection(UID connectionID, StopListener listener, InputBuffer<NetworkToken> buffer) {
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
	}
	
	public void getMails(){
		buffer.addMessageIntoInput(new NetworkToken("Hallo Server", connectionID, config.getServer()));
	}
	public void addMessage(String message){
		System.out.println("Server" + message);
	}
	

}
