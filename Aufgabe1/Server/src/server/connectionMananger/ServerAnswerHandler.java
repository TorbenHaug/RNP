package server.connectionMananger;

import java.rmi.server.UID;
import java.util.Map;

import utils.adt.NetworkToken;
import sun.rmi.runtime.NewThreadAction;
import utils.buffer.InputBuffer;

public class ServerAnswerHandler implements Runnable {
	private boolean isStoped = false;
	private final InputBuffer<NetworkToken> buffer;
	private final Map<UID, ClientConnectionDokument> clientMap;
	
	public ServerAnswerHandler(InputBuffer<NetworkToken> buffer,Map<UID, ClientConnectionDokument> clientMap) {
		this.buffer = buffer;
		this.clientMap = clientMap;
	}
	@Override
	public void run() {
		while(!isStoped){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				ClientConnectionDokument client = clientMap.get(token.getID());
				if (client != null){
					client.sendMessage(token.getMessage());
					if (token.getMessage().equals("OK BYE") || token.getMessage().equals("OK SHUTDOWN")){
						client.stop();
						clientMap.remove(token.getID());
					}
				}
			}
		}
		System.out.println("Answerhandler heruntergefahren");
		
	}
	public void stop(){
		isStoped = true;
	}

}
