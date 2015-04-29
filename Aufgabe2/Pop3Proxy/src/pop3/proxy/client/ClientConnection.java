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

import pop3.proxy.configReader.Config;
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
	private int failStat = 3;
	private String uniqueMailName;
	private int lineCount;
	private long lastExecution;
	private final int maxSize = 5*1024*1024;
	
	public ClientConnection(UID connectionID, Config config, StopListener listener, InputBuffer<NetworkToken> buffer, String mailDrop) {
		this.config = config;
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
		this.mailDrop = mailDrop;
		setLastExecution(System.currentTimeMillis());
		
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
		setLastExecution(System.currentTimeMillis());
				
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
			listener.stop(connectionID);
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
			if (!failLogin()){
				currentState = User;
				sendMessage("USER " + config.getUser());
			}
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void passState(String message){
		if (message.startsWith(ok)){
			currentState = Transaction;
			sendMessage("STAT");
		} else if (message.startsWith(err)){
			if (!failLogin()){
				currentState = User;
				sendMessage("USER " + config.getUser());
			}
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void transactionState(String message){
		if(message.startsWith(ok)){
			String[] splitMessage = message.split(" ", 3);
			if(splitMessage.length == 3){
				System.out.println(splitMessage[1]);
				try{
					numberOfMessages = Long.parseLong(splitMessage[1]);
				}catch(NumberFormatException e){
					failStat--;
					if (failStat <= 0){
						sendMessage("QUIT");
						currentState = Update;
					}else{
						sendMessage("STAT");
					}
				}
				if(!readingStart()){
					//System.out.println("Nothing to Read");
					currentState = Update;
					sendMessage("QUIT");
				}
			}else{
				failStat--;
				if (failStat <= 0){
					sendMessage("QUIT");
					currentState = Update;
				}else{
					sendMessage("STAT");
				}
			}
		}else{
			failStat--;
			if (failStat <= 0){
				sendMessage("QUIT");
				currentState = Update;
			}else{
				sendMessage("STAT");
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
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
	
	/**
	 * 
	 * @param message
	 */
	private void readingState(String message){
		lineCount++;
		if(lineCount == 1){
			if(message.startsWith(err)){
				currentState = Delete;
				deleteState(message);
			}else if(!message.startsWith(ok)){
				lineCount--;
			}
		}else{
			if(!message.equals(".\r\n")){
				writeToFile(message);
			}else{
				sendMessage("DELE " + messageCount);
				currentState = Delete;
			}
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	private void deleteState(String message){
		if(message.startsWith(ok)||message.startsWith(err)){
			if(!readingStart()){
				//System.out.println("Nothing to Read");
				currentState = Update;
				sendMessage("QUIT");
			}
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	private void writeToFile(String message){
		try { 
			File file = new File(ownMailDrop + File.separator + "file" + uniqueMailName + ".txt");
			//System.out.println(" this is the directory of the file: " + file.toString());
			
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
	
	private boolean failLogin(){
		maxOfTry--;
		//System.out.println(maxOfTry);
		if (maxOfTry <= 0){
			sendMessage("QUIT");
			currentState = Update;
			return true;
		}
		return false;
	}

	public synchronized long getLastExecution() {
		return lastExecution;
	}

	public synchronized void setLastExecution(long lastExecution) {
		this.lastExecution = lastExecution;
	}
	
	
	
}
