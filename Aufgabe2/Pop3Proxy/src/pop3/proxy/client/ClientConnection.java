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
import java.util.ArrayList;

import pop3.proxy.configReader.AccountConfig;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ClientConnection{

	private final AccountConfig accountConfig;
	private final StopListener listener;
	private final UID connectionID;
	private final InputBuffer<NetworkToken> buffer;
	private final String ok = "+OK";
	private final String err = "-ERR";
	private final String mailDrop;
	private final String ownMailDrop;
	private ClientState currentState = Connected;
	private int messageCount = 0;
	private int maxOfTry= 5;
	private int failStat = 3;
	private String uniqueMailName;
	private int lineCount;
	private long lastExecution;
	private final int maxSize;
	private ArrayList<Integer> listOfMails;
	private int messageLength;
	
	public ClientConnection(UID connectionID, AccountConfig accountConfig, StopListener listener, InputBuffer<NetworkToken> buffer, String mailDrop, int maxMailSize) {
		this.maxSize = maxMailSize;
		this.accountConfig = accountConfig;
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
		this.mailDrop = mailDrop;
		setLastExecution(System.currentTimeMillis());
		
		this.ownMailDrop = this.mailDrop + File.separator + accountConfig.getUser();
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
		} else if (currentState == List){
			listState(message);
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
			sendMessage("USER " + accountConfig.getUser());
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
			sendMessage("PASS " + accountConfig.getPass());
		} else if (message.startsWith(err)){
			if (!failLogin()){
				currentState = User;
				sendMessage("USER " + accountConfig.getUser());
			}
		}	
	}
	
	/**
	 * 
	 * @param splitMessage
	 */
	private void passState(String message){
		if (message.startsWith(ok)){
			this.listOfMails = new ArrayList<Integer>();
			currentState = List;
			sendMessage("LIST");
		} else if (message.startsWith(err)){
			if (!failLogin()){
				currentState = User;
				sendMessage("USER " + accountConfig.getUser());
			}
		}	
	}
	
	
	
	private void listState(String message){
		if(message.startsWith(ok)){
			currentState = List;
		}else if(message.equals(".\r\n")){
			try{
				if(!readingStart()){
					sendMessage("QUIT");
					currentState = Update;
				}
			}catch(MailToLargeException e){
				System.out.println("Mail of " + accountConfig.getUser() + " Mail size " + listOfMails.get(messageCount-1) + " Mail too long");
				listState(message);
			}
		}else{
			message = message.substring(0, message.length() - 2);
			String[] splitedMessage = message.split(" ");
			if(splitedMessage.length == 2){
				try{
					this.listOfMails.add(Integer.parseInt(splitedMessage[1]));
				}catch (NumberFormatException e){
					this.listOfMails.add(Integer.MAX_VALUE);
				}
			}else{
				this.listOfMails.add(Integer.MAX_VALUE);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws MailToLargeException 
	 */
	private boolean readingStart() throws MailToLargeException{
		
		messageCount++;
		if(listOfMails.size() >= messageCount && listOfMails.get(messageCount-1)>maxSize){
			throw new MailToLargeException();
		}
		this.messageLength = 0;
		if(listOfMails.size()>=messageCount){
			sendMessage("RETR " + (messageCount));
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
				if(!(messageLength > maxSize)){
					sendMessage("DELE " + messageCount);
					currentState = Delete;
				}else{
					try{
						if(!readingStart()){
							sendMessage("QUIT");
							currentState = Update;
						}
					}catch(MailToLargeException e){
						System.out.println("Mail of " + accountConfig.getUser() + " Mail size " + listOfMails.get(messageCount-1) + " Mail too long");
						readingState(message);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	private void deleteState(String message){
		if(message.startsWith(ok)||message.startsWith(err)){
			try{
				if(!readingStart()){
					//System.out.println("Nothing to Read");
					currentState = Update;
					sendMessage("QUIT");
				}
			}catch(MailToLargeException e){
				System.out.println("Mail of " + accountConfig.getUser() + " Mail size " + listOfMails.get(messageCount-1) + " Mail too long");
				deleteState(message);
			}
			
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	private void writeToFile(String message){
		try { 
			this.messageLength += message.length();
			if(messageLength > maxSize){
				File file = new File(ownMailDrop + File.separator + "file" + uniqueMailName + ".txt");
				if(file.exists()){
					file.delete();
					System.out.println("BETRUG!");
				}
			}else{
				File file = new File(ownMailDrop + File.separator + "file" + uniqueMailName + ".txt");
				//System.out.println(" this is the directory of the file: " + file.toString());
				
				if (!file.exists()) {
					file.createNewFile();
				}
				 
				FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.append(message);
				bufferedWriter.close();
			}
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
		setLastExecution(System.currentTimeMillis());
		buffer.addMessageIntoInput(new NetworkToken(message, connectionID, accountConfig.getServer()));
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
