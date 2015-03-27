package server.connectionMananger;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.Date;

import server.adt.NetworkTocken;
import utils.buffer.InputBuffer;

/**

 */
public class Client implements Runnable{

    private final Socket clientSocket;
    private final InputBuffer<NetworkTocken> buffer;
	private Thread runningThread;
	private boolean isStopped = false;
	private final UID clientId;
	InputStream input;

    public Client(Socket clientSocket, InputBuffer<NetworkTocken> buffer) {
        this.clientSocket = clientSocket;
        this.buffer   = buffer;
        this.clientId = new UID();
    }

    public void run() {
    	synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        while(! isStopped()){
			try {
				input = clientSocket.getInputStream();
			} catch (IOException e1) {
				System.out.println((new Date()).toString() +  " Client " + clientId + " disconnected");
				return;
			}
	        try {
	            buffer.addMessageIntoInput(new NetworkTocken(input.toString(), clientId, getIP()));
	            input.close();
	        } catch (IOException e) {
	            //report exception somewhere.
	            e.printStackTrace();
	        }
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

