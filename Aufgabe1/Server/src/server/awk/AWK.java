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
			String inputMessage = token.getMessage();
			
			/* execute command */
			String outputMessage = convertImput(inputMessage);
			
			buffer.addMessageIntoOutput(new NetworkToken(outputMessage, token.getClientID(), token.getClientIP()));
		}
	}
	
	public void stop(){
		
	}
	
	
	private String convertImput(String input){
		String returnMessage;
		
		/* split inputMessage into two parts,
		 * firstpart: command 
		 * second: message  */
		String[] splitString = input.split(" ", 2);
		
		String command = splitString[0];
		
		if(command.equals("BYE")){
			/* do what you have to do here */
		}
		else if (splitString.length < 2){
			return returnMessage = "ERROR SYNTAX ERROR: MESSAGE OR PASSWORD IS MISSING";
		}
		
		String message = splitString[1];
		
		switch (command){				
			case "LOWERCASE":	returnMessage = convertToLowercase(message);
								break;
			case "UPPERCASE": 	returnMessage = convertToUppercase(message);
								break;
			case "REVERSE":		returnMessage = convertToReverseString(message);
								break;
			case "SHUTDOWN":						
			default: 			returnMessage = "ERROR UNKNOWN COMMAND";
								break;
		}
		
		return returnMessage;
		
	}
	
	
	
	private String convertToLowercase(String text){
		return text.toLowerCase();
	}
	
	
	private String convertToUppercase(String text){
		return text.toUpperCase();
	}
	
	private String convertToReverseString(String text){
		return new StringBuilder(text).reverse().toString();
		
	}

}
