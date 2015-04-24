package pop3.proxy.client;

import java.rmi.server.UID;
import java.util.Set;
import java.util.UUID;

import client.connectionManager.ClientConnectionManager;
import pop3.proxy.configReader.Config;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ClientConnection{

	private final Config config;
	private final StopListener listener;
	private final UID connectionID;
	private final InputBuffer<NetworkToken> buffer;
	
	public ClientConnection(UID connectionID, Config config, StopListener listener, InputBuffer<NetworkToken> buffer) {
		this.config = config;
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
	}
	
//	public void getMails(){
//		buffer.addMessageIntoInput(new NetworkToken("Hallo Server", connectionID, config.getServer()));
//	}
	synchronized public void addMessage(String message){
		System.out.println("Server" + message);
		
//		stoppt verbindung
//		listener.stop(connectionID);
		
	}
	

}
