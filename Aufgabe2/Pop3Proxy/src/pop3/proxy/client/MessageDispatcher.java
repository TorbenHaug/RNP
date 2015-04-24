package pop3.proxy.client;

import java.rmi.server.UID;
import java.util.Map;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class MessageDispatcher implements Runnable{
	
	private final InputBuffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	public MessageDispatcher(InputBuffer<NetworkToken> buffer, Map<UID, ClientConnection> connections) {
		this.buffer = buffer;
		this.connections = connections;
	}
	@Override
	public void run() {
		while(true){
			NetworkToken token = buffer.getMessageFromOutput();
			ClientConnection connection = connections.get(token.getID());
			if (connection != null){
				connection.addMessage(token.getMessage());
			}
		}
		
	}
	
	public void stop(){
		
	}

}
