package pop3.proxy.client;

import static pop3.proxy.client.ClientState.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.server.UID;
import java.util.Set;
import java.util.UUID;

import client.connectionManager.ClientConnectionManager;
import pop3.proxy.configReader.Config;
import server.awk.State;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ClientConnection{

	private final Config config;
	private final StopListener listener;
	private final UID connectionID;
	private final InputBuffer<NetworkToken> buffer;
	private final String ok = "+OK ";
	private final String ok_crnl = "+OK\r\n";
	private final String err = "-ERR ";
	private final String err_crnl = "-ERR\r\n";
	private ClientState currentState = Connected;
	private long numberOfMessages;
	private long messageCount = 1;
	private long uniqueFileNumber = 0;
	private int maxOfTry= 5;
	private static String correntDir = System.getProperty("user.dir");
	private static final String filePath = correntDir + File.separator + ".." + File.separator + "doc" + File.separator;
	
	public ClientConnection(UID connectionID, Config config, StopListener listener, InputBuffer<NetworkToken> buffer) {
		this.config = config;
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
	}
	
	/**
	 * The method addMessage(String message) handles the input message. It goes from state to state:
	 * Connected -> User -> Pass -> Transaction -> Reading -> Update. In the Reading state it saves 
	 * the input message. 
	 * @param message  the message which has to be handled 
	 */
	synchronized public void addMessage(String message){
		System.out.println("Server" + message);
		String[] splitMessage = message.split(" ", 2);
				
		if (currentState == Connected){
			connectingState(splitMessage);	
		} else if (currentState == User){
			userState(splitMessage);
		} else if (currentState == Pass){
			passState(splitMessage);
		} else if (currentState == Transaction){
			transactionState(splitMessage);
		} else if (currentState == Reading){
			readingState(splitMessage, message);			
		} else if (currentState == Update){
			if ((splitMessage[0].equals(ok))){
				listener.stop(connectionID);
			} else if (splitMessage[0].equals(err)){
				
			}	
		}
		
		
//		stoppt verbindung
//		listener.stop(connectionID);
		
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void connectingState(String[] splitMessage){
		if ((splitMessage[0].equals(ok)) || (splitMessage[0].equals(ok_crnl))){
			currentState = User;
			sendMessage("USER " + config.getUser());
		} else if (splitMessage[0].equals(err) || splitMessage[0].equals(err_crnl)){
			//TODO What happens if connection failed
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void userState(String[] splitMessage){
		if ((splitMessage[0].equals(ok)) || (splitMessage[0].equals(ok_crnl))){
			currentState = Pass;
			sendMessage("PASS " + config.getPass());
		} else if (splitMessage[0].equals(err) || splitMessage[0].equals(err_crnl)){
			currentState = Connected;
			sendMessage("USER " + config.getUser());
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void passState(String[] splitMessage){
		if ((splitMessage[0].equals(ok)) || (splitMessage[0].equals(ok_crnl))){
			currentState = Transaction;
			maxOfTry--;
			sendMessage("STAT");
		} else if (splitMessage[0].equals(err) || splitMessage[0].equals(err_crnl)){
			currentState = Connected;
			sendMessage("USER " + config.getUser());
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void transactionState(String[] splitMessage){
		if ((splitMessage[0].equals(ok)) || (splitMessage[0].equals(ok_crnl))){
			this.numberOfMessages = Long.parseLong(splitMessage[1]);
			if(numberOfMessages > 0){
				sendMessage("RETR " + messageCount);
				uniqueFileNumber++;
			}
			currentState = Reading;
		} else if (splitMessage[0].equals(err) || splitMessage[0].equals(err_crnl)){
			if (maxOfTry > 0){
				maxOfTry--;
				sendMessage("STAT");
			} else {
				System.out.println("Tried to many times to type STAT");
				currentState = Update;
				sendMessage("QUIT");
			}
			
		} 
	}
	
	/**
	 * 
	 * @param splitMessage
	 * @param message
	 */
	private void readingState(String[] splitMessage, String message){
		if ((splitMessage[0].equals(ok)) || (splitMessage[0].equals(ok_crnl))){
		} else if (this.numberOfMessages >= this.messageCount){
			if(!message.equals(".\r\n")){
				writeToFile(message);
			} else {
				sendMessage("DELE " + messageCount);
				messageCount++;
				uniqueFileNumber++;
				sendMessage("RETR " + messageCount);
//				messageCount++;
				
			}
		} else {
			System.out.println("No more Messages to read ");
			currentState = Update;
			sendMessage("QUIT");
		}
			
	}
	
	/**
	 * 
	 * @param message
	 */
	private void writeToFile(String message){
		try { 
			File file = new File(filePath + "file" + uniqueFileNumber + ".txt");
			System.out.println(" this is the directory of the file: " + file.toString());
			
			if (!file.exists()) {
				file.createNewFile();
			}
			 
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append(message);
			bufferedWriter.close();
			
		} catch (IOException e) {
			System.out.println("something went wrong when writing to file " + e);
		}
	}
	
	

	private void sendMessage(String message){
		buffer.addMessageIntoInput(new NetworkToken(message, connectionID, config.getServer()));
	}

}
