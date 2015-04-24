package client.connectionManager;

import java.rmi.server.UID;
import java.util.Map;

import utils.adt.NetworkToken;
import utils.buffer.OutputBuffer;

public class ClientAnswerHandler implements Runnable {
	private boolean isStoped = false;
	private final OutputBuffer<NetworkToken> buffer;
	private final Map<UID, Connection> connectionMap;
	
	public ClientAnswerHandler(OutputBuffer<NetworkToken> buffer,Map<UID, Connection> clientMap) {
		this.buffer = buffer;
		this.connectionMap = clientMap;
	}
	@Override
	public void run() {
		while(!isStoped){
			NetworkToken token = buffer.getMessageFromInput();
			if(token != null && !isStoped){
				Connection client = connectionMap.get(token.getID());
				if (client != null){
					client.sendMessage(token.getMessage());
				}
			}
			else{
				break;
			}
		}
		System.out.println("Answerhandler heruntergefahren");
		
	}
	public void stop(){
		isStoped = true;
	}

}
