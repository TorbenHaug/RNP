package pop3.proxy.client;

import static pop3.proxy.client.ClientState.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private final String ok = "+OK";
	private final String err = "-ERR";
	private final String mailDrop;
	private final String ownMailDrop;
	private ClientState currentState = Connected;
	private long numberOfMessages;
	private long messageCount = 0;
	private int maxOfTry= 5;
	private String uniqueMailName;
	private int lineCount;
	private static String correntDir = System.getProperty("user.dir");
	private static final String filePath = correntDir + File.separator + ".." + File.separator + "doc" + File.separator;
	
	public ClientConnection(UID connectionID, Config config, StopListener listener, InputBuffer<NetworkToken> buffer, String mailDrop) {
		this.config = config;
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
		this.mailDrop = mailDrop;
		
		this.ownMailDrop = this.mailDrop + File.separator + config.getUser();
		if(Files.notExists(Paths.get(ownMailDrop))){
			try {
				Files.createDirectory(Paths.get(ownMailDrop));
			} catch (IOException e) {
				sendMessage("-ERR Internal Server Error");
				listener.stop(connectionID);
			}
		}
	}
	
	/**
	 * The method addMessage(String message) handles the input message. It goes from state to state:
	 * Connected -> User -> Pass -> Transaction -> Reading -> Update. In the Reading state it saves 
	 * the input message. 
	 * @param message  the message which has to be handled 
	 */
	synchronized public void addMessage(String message){
		System.out.println("Server" + message);
				
		if (currentState == Connected){
			connectingState(message);	
		} else if (currentState == User){
			userState(message);
		} else if (currentState == Pass){
			passState(message);
		} else if (currentState == Transaction){
			transactionState(message);
		} else if (currentState == Reading){
			readingState(message);	
		} else if (currentState == Delete){
			deleteState(message);
		} else if (currentState == Update){
			listener.stop(connectionID);	
		}
		
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void connectingState(String message){
		if (message.startsWith(ok)){
			currentState = User;
			sendMessage("USER " + config.getUser());
		} else if (message.startsWith(err)){
			//TODO What happens if connection failed
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void userState(String message){
		if (message.startsWith(ok)){
			currentState = Pass;
			sendMessage("PASS " + config.getPass());
		} else if (message.startsWith(err)){
			currentState = Connected;
			sendMessage("USER " + config.getUser());
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void passState(String message){
		if (message.startsWith(ok)){
			currentState = Transaction;
			maxOfTry--;
			sendMessage("STAT");
		} else if (message.startsWith(err)){
			currentState = Connected;
			sendMessage("USER " + config.getUser());
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void transactionState(String message){
		if(message.startsWith(ok)){
			String[] splitMessage = message.split(" ", 3);
			numberOfMessages = Long.parseLong(splitMessage[1]);
			if(!readingStart()){
				System.out.println("Nothing to Read");
				currentState = Update;
				sendMessage("QUIT");
			}
		}else{
			sendMessage("-ERR Unknown command");
		}
	}
	
	private boolean readingStart(){
		if(numberOfMessages>messageCount){
			sendMessage("RETR " + (++messageCount));
			this.lineCount = 0;
			try {
				uniqueMailName = md5Hash(new UID().toString() + new UID().toString() + new UID().toString());
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				
			}
			currentState = Reading;
			return true;
		}else{
			return false;
		}
	}
	
	private void readingState(String message){
		lineCount++;
		if(lineCount == 1){
			if(!message.startsWith(ok)){
				currentState = Delete;
			}
		}else{
			if(!message.equals(".\r\n")){
				writeToFile(message);
			}else{
				sendMessage("DELE" + messageCount);
				currentState = Delete;
			}
		}
	}
	private void deleteState(String message){
		if(!readingStart()){
			System.out.println("Nothing to Read");
			currentState = Update;
			sendMessage("QUIT");
		}
	}
//	private void transactionState(String message){
//		String[] splitMessage = message.split(" ", 3);
//		System.out.println(splitMessage[0]);
//		System.out.println(splitMessage[1]);
//		System.out.println(splitMessage[2]);
//		if (message.startsWith(ok)){
//			this.numberOfMessages = Long.parseLong(splitMessage[1]);
//			if(numberOfMessages > 0){
//				sendMessage("RETR " + (messageCount + 1));
//				try {
//					uniqueMailName = md5Hash(new UID().toString() + new UID().toString() + new UID().toString());
//				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//			}
//			currentState = Reading;
//		} else if (message.startsWith(err)){
//			if (maxOfTry > 0){
//				maxOfTry--;
//				sendMessage("STAT");
//			} else {
//				System.out.println("Tried to many times to type STAT");
//				currentState = Update;
//				sendMessage("QUIT");
//			}
//			
//		} 
//	}
//	
//	/**
//	 * 
//	 * @param splitMessage
//	 * @param message
//	 */
//	private void readingState(String message){
//		if (message.startsWith(ok)){
//			currentState = DuringReadingState;
//		} else if (this.numberOfMessages > this.messageCount){
//			sendMessage("RETR " + (messageCount + 1));
//			try {
//				uniqueMailName = md5Hash(new UID().toString() + new UID().toString() + new UID().toString());
//			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		} else {
//			System.out.println("No more Messages to read ");
//			currentState = Update;
//			sendMessage("QUIT");
//		}
//			
//	}
//	
//	private void duringReadingState(String message){
//		if(!message.equals(".\r\n")){
//			writeToFile(message);
//		} else {
//			sendMessage("DELE " + (messageCount + 1));
//			messageCount++;
//			if (this.numberOfMessages > this.messageCount){
//				currentState = Reading;
//			} else {
//				currentState = Update;
//			}
//			
//		}
//	}
	
	/**
	 * 
	 * @param message
	 */
	private void writeToFile(String message){
		try { 
			File file = new File(ownMailDrop + File.separator + "file" + uniqueMailName + ".txt");
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
	
	/**
	 * 
	 * @param message
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private String md5Hash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
        
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < hashedBytes.length; i++) {
            stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
	}

	private void sendMessage(String message){
		buffer.addMessageIntoInput(new NetworkToken(message, connectionID, config.getServer()));
	}

}
