package client.gui;

import client.controller.Controller;
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
		while(!isStopped){
			NetworkToken token = buffer.getMessageFromOutput();
			if(token != null){
				String msg = token.getMessage();
				System.out.print(msg);
				if(msg.equals("OK BYE\n") || msg.equals("OK SHUTDOWN\n")){
					Controller.shutdown();
				}
			}
		}
		System.out.println("Output wurde beendet");
		
	}
	public void stop(){
		isStopped = true;
	}

}
