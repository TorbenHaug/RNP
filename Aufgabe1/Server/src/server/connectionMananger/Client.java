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

import server.adt.NetworkToken;
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

    public Client(Socket clientSocket, InputBuffer<NetworkToken> buffer) {
        this.clientSocket = clientSocket;
        this.buffer   = buffer;
        this.clientId = new UID();
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
        while(! isStopped()){
			try {
				String line = "";
				int sign = 0;
				while ((sign = input.read()) != -1) {
				    line+= (char) sign;
				    if (line.length() >= 255 || (char) sign == '\n'){
				    	buffer.addMessageIntoInput(new NetworkToken(line, clientId, getIP()));
				    	line = "";
				    }
				}
				
				//input = clientSocket.getInputStream();
				System.out.println(input.toString());
			} catch (IOException e1) {
				System.out.println((new Date()).toString() +  " Client " + clientId + " disconnected");
				return;
			}
	        //try {
	            
	            //input.close();
	        //} catch (IOException e) {
	            //report exception somewhere.
	           // e.printStackTrace();
	        //}
	    }
        System.out.println((new Date()).toString() +  " Client " + clientId + " disconnected");
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
			e.printStackTrace();
		}
         
	}
	public void stop(){
		isStopped = true;
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

