package server.awk;

import java.rmi.server.UID;
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
	
	public ClientConnection(UID connectionID, StopListener listener, OutputBuffer<NetworkToken> buffer, int timeOut, CheckUser checkUser) {
		this.listener = listener;
		this.connectionID = connectionID;
		this.buffer = buffer;
		this.timeOut = timeOut * 1000;
		this.lastUse = System.currentTimeMillis(); 
		this.checkUser = checkUser;
		this.failedLogins = 0;
		sendMessage("+OK Hello to the pop3server of Louisa and Torben.");
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
			}else{
				sendMessage("-ERR Wrong Command");
			}
		}else if(currentState == Pass){
			if(message.startsWith("PASS ")){
				if((config = checkUser.checkPass(userName, message.substring(message.indexOf(" ") + 1, message.length()))) != null){
					currentState = LoggedIn;
					sendMessage("+OK Welcome " + userName + ".");
				}
				else{
					currentState = Connected;
					sendMessage("-ERR Password doesn't match.");
					failLogin();
				}
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
