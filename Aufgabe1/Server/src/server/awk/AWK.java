package server.awk;

import server.adt.NetworkToken;
import utils.buffer.OutputBuffer;

public class AWK implements Runnable{
	
	private final OutputBuffer<NetworkToken> buffer;
	private boolean isStopped = false;
	public AWK(OutputBuffer<NetworkToken> buffer){
		this.buffer = buffer;
	}
	@Override
	public void run() {
		while(!isStopped){
			NetworkToken token = buffer.getMessageFromInput();
			
			buffer.addMessageIntoOutput(new NetworkToken("Acc", token.getClientID(), token.getClientIP()));
		}
		
	}
	
	public void stop(){
		
	}
	
	
	
	
	private String convertToLowercase(String text){
		String str = "hallo";
		return str;
	}

}
