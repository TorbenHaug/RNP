package server.connectionMananger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.Date;
import java.util.Map;

import javax.sound.midi.ControllerEventListener;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

/**

 */
public class ClientConnectionDokument implements Runnable{

    private final Socket clientSocket;
    private final InputBuffer<NetworkToken> buffer;
	private Thread runningThread;
	private boolean isStopped = false;
	private final UID clientId;
	private BufferedReader input;
	private boolean isDown;
	private final Map<UID, ClientConnectionDokument> clientMap;
	private final int maxLineSize;

    public ClientConnectionDokument(Socket clientSocket, InputBuffer<NetworkToken> buffer,Map<UID, ClientConnectionDokument> clientMap, int maxLineSize) {
        this.maxLineSize = maxLineSize;
    	this.clientSocket = clientSocket;
        this.buffer   = buffer;
        this.clientId = new UID();
        this.clientMap = clientMap;
        try {
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
		} catch (IOException e) {
			stop();
		}
        buffer.addMessageIntoInput(new NetworkToken("CONN", clientId, clientSocket.getInetAddress().toString()));
    }

    public void run() {
    	synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		try {
			String line = "";
			int sign = 0;
			while (((sign = input.read()) != -1) && !isStopped) {
			    line+= (char) sign;
			    if (line.length() >= maxLineSize){
			    	sendMessage("ERROR Message too long");
			    	while (((sign = input.read()) != -1) && (((char) sign) != '\n') && !isStopped){
			    		
			    	}
			    	line = "";
			    }
			    else if (line.endsWith("\r\n")){
			    	buffer.addMessageIntoInput(new NetworkToken(line, clientId, getIP()));
			    	line = "";
			    }
			}
		} catch (IOException e) {
			buffer.addMessageIntoInput(new NetworkToken("QUIT", clientId, getIP()));
		} finally {
			stop();
			clientMap.remove(clientId);
			isDown = true;
			System.out.println((new Date()).toString() +  " Client " + clientId + " disconnected");
		}
	       
    }

	public String getIP() {
		return clientSocket.getInetAddress().toString();
	}
	
	private synchronized boolean isStopped() {
        return this.isStopped ;
    }

	public UID getClientId() {
		return clientId;
	}
	public synchronized void sendMessage(String message){
		message += "\r\n";
		OutputStream output;
		try {
			output = clientSocket.getOutputStream();
			output.write(message.getBytes("UTF-8"));
			//output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
         
	}
	public void stop(){
		isStopped = true;
		try {
			//input.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean isDown(){
		return isDown;
	}
}

