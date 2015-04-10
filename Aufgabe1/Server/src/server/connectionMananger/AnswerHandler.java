package server.connectionMananger;

import java.rmi.server.UID;
import java.util.Map;

import server.adt.NetworkToken;
import sun.rmi.runtime.NewThreadAction;
import utils.buffer.InputBuffer;

public class AnswerHandler implements Runnable {
	private boolean isStoped = false;
	private final InputBuffer<NetworkToken> buffer;
	private final Map<UID, Client> clientMap;
	
	public AnswerHandler(InputBuffer<NetworkToken> buffer,Map<UID, Client> clientMap) {
		this.buffer = buffer;
		this.clientMap = clientMap;
	}
	@Override
	public void run() {
		while(!isStoped){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				Client client = clientMap.get(token.getClientID());
				if (client != null){
					client.sendMessage(token.getMessage());
					if (token.getMessage().equals("OK BYE") || token.getMessage().equals("OK SHUTDOWN")){
						client.stop();
						clientMap.remove(token.getClientID());
					}
				}
			}
		}
		
	}
	public void stop(){
		isStoped = true;
	}

}
