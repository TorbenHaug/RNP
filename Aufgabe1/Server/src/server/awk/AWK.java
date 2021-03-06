package server.awk;

import server.controller.ServerController;
import utils.adt.NetworkToken;
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
			
			if(token != null){			
				String inputMessage = token.getMessage();
				System.out.print(token.getID().toString() + " " + inputMessage);
				/* execute command */
				String outputMessage = convertImput(inputMessage);
				
				buffer.addMessageIntoOutput(new NetworkToken(outputMessage, token.getID(), token.getIP()));
			}
		}
		System.out.println("AWK Heruntergefahren");
	}
	
	public void stop(){
		isStopped = true;
	}
	
	/**
	 * converts the input string to the intended output string
	 * @param input  string which has to be converted
	 * @return  converted input
	 */
	private String convertImput(String input){
		String returnMessage;
		
		/* split inputMessage into two parts,
		 * firstpart: command 
		   more than one space is allowed to be in the input. 
		   Possible regex use is also "\\s+" */
		String[] splitString = input.split(" +", 2); 
		
		String command = splitString[0];
		
		/* If empty string is given*/ 
		if(command.equals("\n")){
			return returnMessage = "ERROR SYNTAX ERROR: EMPTY STRING";
		}
		
		/* Command input BYE
		 * if input is correct: than client disconnect
		 * if inout is incorrect because
		 *    command != BYE 
		 *    command == BYE && argument input*/
		if(command.equals("BYE\n")){
			if(splitString.length == 1){
				return returnMessage = "OK BYE";
			}else {
				return returnMessage = " ERROR SNTAX ERROR: COMMAND 'BYE' DOES NOT EXPECT PARAMETERS";
			}
		}else if (splitString.length < 2){
				return returnMessage = "ERROR SYNTAX ERROR: UNKNOWN COMMAND OR MESSAGE OR PASSWORD IS MISSING";
		}
		
		String message = splitString[1];
		
		switch (command){				
			case "LOWERCASE":	returnMessage = convertToLowercase(message);
								break;
			case "UPPERCASE": 	returnMessage = convertToUppercase(message);
								break;
			case "REVERSE":		returnMessage = convertToReverseString(message);
								break;
			case "SHUTDOWN":	returnMessage = shutdownServer(message);
								break;
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
	
	private String shutdownServer(String password){
		boolean bool = ServerController.shutdown(password);
		
		if(bool){
			return "OK SHUTDOWN";
		}else{
			return "ERROR SERVER COULD NOT SHUTDOWN PASSWORD WRONG";
		}
		
	}

}
