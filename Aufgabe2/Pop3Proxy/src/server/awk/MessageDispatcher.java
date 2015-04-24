package server.awk;

import java.rmi.server.UID;
import java.util.Map;

import pop3.proxy.client.StopListener;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;
import utils.buffer.OutputBuffer;

public class MessageDispatcher implements Runnable{
	
	private final OutputBuffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	private boolean isStopped = false;
	private final CreateClientConnection create;
	private final StopListener clientDisconnector;
	
	public MessageDispatcher(OutputBuffer<NetworkToken> buffer, Map<UID, ClientConnection> connections,StopListener clientDisconnector, CreateClientConnection create) {
		this.buffer = buffer;
		this.connections = connections;
		this.create = create;
		this.clientDisconnector = clientDisconnector;
	}
	@Override
	public void run() {
		while(!isStopped ){
			NetworkToken token = buffer.getMessageFromInput();
			if(token != null){
				System.out.println(token.getMessage());
				if(token.getMessage().equals("CONN")){
					if(!create.createClient(token)){
						clientDisconnector.stop(token.getID());
					}
				}else{
					ClientConnection connection = connections.get(token.getID());
					if (connection != null){
						connection.addMessage(token.getMessage());
					}
				}
			}
		}
		System.out.println("Stoped MessageDispatcher");
		
	}
	
	public void stop(){
		isStopped = true;
	}

}