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

import server.adt.NetworkToken;
import server.controller.Controller;
import utils.buffer.InputBuffer;

/**

 */
public class Client implements Runnable{

    private final Socket clientSocket;
    private final InputBuffer<NetworkToken> buffer;
	private Thread runningThread;
	private boolean isStopped = false;
	private final UID clientId;
	BufferedReader input;
	private boolean isDown;
	private final Map<UID, Client> clientMap;

    public Client(Socket clientSocket, InputBuffer<NetworkToken> buffer,Map<UID, Client> clientMap) {
        this.clientSocket = clientSocket;
        this.buffer   = buffer;
        this.clientId = new UID();
        this.clientMap = clientMap;
        try {
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void run() {
    	synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		try {
			String line = "";
			int sign = 0;
			while ((sign = input.read()) != -1) {
			    line+= (char) sign;
			    if (line.length() >= 255 || (char) sign == '\n'){
			    	buffer.addMessageIntoInput(new NetworkToken(line, clientId, getIP()));
			    	line = "";
			    	Controller.setUsed();
			    }
			}
			
			//input = clientSocket.getInputStream();
			//System.out.println(input.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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
		OutputStream output;
		try {
			output = clientSocket.getOutputStream();
			output.write(message.getBytes());
			output.close();
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

