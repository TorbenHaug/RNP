package pop3.proxy.client;

import java.rmi.server.UID;
import java.util.Map;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class MessageDispatcher implements Runnable{
	
	private final InputBuffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	private boolean isStopped = false;
	public MessageDispatcher(InputBuffer<NetworkToken> buffer, Map<UID, ClientConnection> connections) {
		this.buffer = buffer;
		this.connections = connections;
	}
	@Override
	public void run() {
		while(!isStopped ){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				ClientConnection connection = connections.get(token.getID());
				if (connection != null){
					connection.addMessage(token.getMessage());
				}
			}
		}
		System.out.println("Stoped Client MessageDispatcher");
		
	}
	
	public void stop(){
		isStopped = true;
		
	}

}
