package client.gui;

import client.controller.ClientController;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class GUIOutput implements Runnable{

	private boolean isStopped = false;
	private final InputBuffer<NetworkToken> buffer;
	
	public GUIOutput(InputBuffer<NetworkToken> buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void run() {
		System.out.println("Use CONNECT <Adress> <Port> to connect to Server");
		while(!isStopped){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				String msg = token.getMessage();
				System.out.print(msg);
				if(msg.equals("OK BYE\n")|| msg.equals("OK SHUTDOWN\n")){
					ClientController.disconnectCurrentConnection();
					System.out.println("Use CONNECT <address> <port> to reconnect");
				}
			}
		}
		System.out.println("Output wurde beendet");
		
	}
	public void stop(){
		isStopped = true;
	}

}
