package server.awk;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static server.awk.State.*;
import client.connectionManager.ClientConnectionManager;
import pop3.proxy.client.StopListener;
import pop3.proxy.configReader.Config;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;
import utils.buffer.OutputBuffer;

public class ClientConnection implements Runnable{

	private Config config;
	private final StopListener listener;
	private final UID connectionID;
	private final OutputBuffer<NetworkToken> buffer;
	private long lastUse;
	private final int timeOut;
	private State currentState = Connected;
	private final CheckUser checkUser;
	private int failedLogins;
	private String userName = "";
	private final Map<UID, String> lookedUsers;
	private final String mailDrop;
	private String ownMailDrop;
	private List<MailWrapper> currentMails = new ArrayList<>();
	
	public ClientConnection(UID connectionID, StopListener listener, OutputBuffer<NetworkToken> buffer, int timeOut, CheckUser checkUser, Map<UID, String> lookedUsers,String mailDrop) {
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
		this.timeOut = timeOut * 1000;
		this.lastUse = System.currentTimeMillis(); 
		this.checkUser = checkUser;
		this.failedLogins = 0;
		sendMessage("+OK Hello to the pop3server of Louisa and Torben.");
		this.lookedUsers = lookedUsers;
		this.mailDrop = mailDrop;
	}
	
	public void addMessage(String message){
		lastUse = System.currentTimeMillis();
		if(!message.endsWith("\r\n")){
			sendMessage("-ERR Unknown Format");
			return;
		}
		message = message.substring(0, message.length() - 2);
		if(currentState == Connected){
			if(message.startsWith("USER ")){
				if(checkUser.userExists((userName = message.substring(message.indexOf(" ") + 1, message.length())))){
					currentState = Pass;
					sendMessage("+OK Please give me your password");
				}
				else{
					currentState = Connected;
					sendMessage("-ERR User didn't exists");
					failLogin();
				}
			}else if(message.startsWith("QUIT")){
				listener.stop(connectionID);
			}else{
				sendMessage("-ERR Wrong Command");
			}
		}else if(currentState == Pass){
			if(message.startsWith("PASS ")){
				if((config = checkUser.checkPass(userName, message.substring(message.indexOf(" ") + 1, message.length()))) != null){
					if(!lookedUsers.containsValue(config.getUser())){
						lookedUsers.put(connectionID, config.getUser());
						currentState = LoggedIn;
						ownMailDrop = mailDrop + File.separator + config.getUser();
						if(Files.notExists(Paths.get(ownMailDrop))){
							try {
								Files.createDirectory(Paths.get(ownMailDrop));
							} catch (IOException e) {
								sendMessage("-ERR Internal Server Error");
								listener.stop(connectionID);
							}
						}
				        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(ownMailDrop))) {
				            for (Path path : directoryStream) {
				            	String extension = "";
				            	int lastPoint = path.toString().lastIndexOf('.');
				            	if (lastPoint > 0) {
				            	    extension = path.toString().substring(lastPoint+1);
				            	}
				            	if(path.toFile().isFile() && extension.equals("txt")){
				            		currentMails.add(new MailWrapper(path.toFile()));
				            	}
				            }
				        } catch (IOException | NoSuchAlgorithmException ex) {
				        	sendMessage("-ERR Internal Server Error");
							listener.stop(connectionID);
				        }
				        
						sendMessage("+OK Welcome " + userName + ".");
					}else{
						currentState = Connected;
						sendMessage("-ERR Unable to lock Maildrop");
					}
					
				}
				else{
					currentState = Connected;
					sendMessage("-ERR Password doesn't match.");
					failLogin();
				}
			}else if(message.startsWith("QUIT")){
				listener.stop(connectionID);
			}else{
				sendMessage("-ERR Wrong Command");
			}
		}else if(currentState == LoggedIn){
			if(message.equals("STAT")){
				long combinedLength = 0;
				int size = 0;
				for (MailWrapper mail: currentMails){
					if(!mail.isDeleted()){
						size++;
						combinedLength += mail.getMail().length();
					}
				}
				sendMessage("+OK " + size + " " + combinedLength);
			}else if(message.equals("LIST")){
				long combinedLength = 0;
				int size = 0;
				for (MailWrapper mail: currentMails){
					if(!mail.isDeleted()){
						size++;
						combinedLength += mail.getMail().length();
					}
				}
				sendMessage("+OK " + size + " messages (" + combinedLength + " octets)");
				for(int i = 0; i < currentMails.size(); i++){
					if(!currentMails.get(i).isDeleted()){
						sendMessage((i+1) + " " + currentMails.get(i).getMail().length());
					}
				}
				sendMessage(".");
			}else if(message.startsWith("LIST ")){
				String msgId = message.substring(message.indexOf(" ") + 1, message.length());
				if(msgId.isEmpty()){
					sendMessage("-ERR No MessageID found");
				}else{
					try{
						int intId = Integer.valueOf(msgId);
						if(intId > currentMails.size() || currentMails.get(intId - 1).isDeleted() || intId < 1){
							sendMessage("-ERR no such message");
						}else{
							sendMessage("+OK " + intId + " " + currentMails.get(intId - 1).getMail().length());
						}
					}catch(NumberFormatException e){
						sendMessage("-ERR " + msgId + " is not a number");
					}
				}
			}else if(message.startsWith("RETR ")){
				String msgId = message.substring(message.indexOf(" ") + 1, message.length());
				if(msgId.isEmpty()){
					sendMessage("-ERR No MessageID found");
				}else{
					try{
						int intId = Integer.valueOf(msgId);
						if(intId > currentMails.size() || currentMails.get(intId - 1).isDeleted() || intId < 1){
							sendMessage("-ERR no such message");
						}else{
							sendMessage("+OK message follows");
							for(String part: currentMails.get(intId - 1).getSplitedMail()){
								sendMessage(part);
							}
							sendMessage(".");
						}
					}catch(NumberFormatException e){
						sendMessage("-ERR " + msgId + " is not a number");
					}
				}
			}else if(message.startsWith("DELE ")){
				String msgId = message.substring(message.indexOf(" ") + 1, message.length());
				if(msgId.isEmpty()){
					sendMessage("-ERR No MessageID found");
				}else{
					try{
						int intId = Integer.valueOf(msgId);
						if(intId > currentMails.size() || currentMails.get(intId - 1).isDeleted() || intId < 1){
							sendMessage("-ERR no such message");
						}else{
							currentMails.get(intId - 1).setDeleted(true);
							sendMessage("+OK message " + intId + " deleted");
						}
					}catch(NumberFormatException e){
						sendMessage("-ERR " + msgId + " is not a number");
					}
				}
			}else if(message.startsWith("NOOP ")){
				sendMessage("+OK");
			}else if(message.equals("RSET")){
				for(MailWrapper mail: currentMails){
					mail.setDeleted(false);
				}
				sendMessage("+OK");
			}else if(message.equals("UIDL")){
				sendMessage("+OK");
				for(int i=0; i<currentMails.size();i++){
					sendMessage((i + 1) + " " + currentMails.get(i).getMd5hash());
				}
				sendMessage(".");
			}else if(message.startsWith("UIDL ")){
				String msgId = message.substring(message.indexOf(" ") + 1, message.length());
				if(msgId.isEmpty()){
					sendMessage("-ERR No MessageID found");
				}else{
					try{
						int intId = Integer.valueOf(msgId);
						if(intId > currentMails.size() || currentMails.get(intId - 1).isDeleted() || intId < 1){
							sendMessage("-ERR no such message");
						}else{
							currentMails.get(intId - 1).setDeleted(true);
							sendMessage("+OK " + intId + " " + currentMails.get(intId - 1).getMd5hash());
						}
					}catch(NumberFormatException e){
						sendMessage("-ERR " + msgId + " is not a number");
					}
				}
			}else if(message.startsWith("QUIT")){
				listener.stop(connectionID);
			}else{
				sendMessage("-ERR Wrong Command");
			}
		}
	}

	private void failLogin() {
		failedLogins++;
		if(failedLogins > 3){
			listener.stop(connectionID);
		}
		
	}

	@Override
	public void run() {
		long timeLast = timeOut;
		while((timeLast = ((lastUse - System.currentTimeMillis()) + timeOut)) > 0){
			System.out.println(timeLast);
			try {
				Thread.sleep(timeLast);
			} catch (InterruptedException e) {
				listener.stop(connectionID);
			}
		}
		listener.stop(connectionID);
	}
	private void sendMessage(String message){
		buffer.addMessageIntoOutput(new NetworkToken(message, connectionID, "0.0.0.0"));
	}
	

}
