package server.awk;

import java.rmi.server.UID;
import java.util.Map;

import pop3.proxy.client.StopListener;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class MessageDispatcher implements Runnable{
	
	private final InputBuffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	private boolean isStopped = false;
	private final CreateClientConnection create;
	private final StopListener clientDisconnector;
	
	public MessageDispatcher(InputBuffer<NetworkToken> buffer, Map<UID, ClientConnection> connections,StopListener clientDisconnector, CreateClientConnection create) {
		this.buffer = buffer;
		this.connections = connections;
		this.create = create;
		this.clientDisconnector = clientDisconnector;
	}
	@Override
	public void run() {
		while(!isStopped ){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				if(token.getMessage().equals("CONN")){
					if(create.createClient(token)){
						buffer.addMessageIntoInput(new NetworkToken("+OK Hello to the server Louisa and Torben.", token.getID(), token.getIP()));
					}
					else{
						buffer.addMessageIntoInput(new NetworkToken("-ERR Hello from server of Louisa and Torben, we are sorry to but something went wrong", token.getID(), token.getIP()));
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