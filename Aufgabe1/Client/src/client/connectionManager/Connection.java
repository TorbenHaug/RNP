package client.connectionManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Date;
import java.util.Map;

import client.controller.ClientController;
import utils.adt.NetworkToken;
import utils.buffer.OutputBuffer;

public class Connection implements Runnable{
	private Socket socket;
	private UID uid;
	private boolean isStopped = false;
	private BufferedReader input;
	private final OutputBuffer<NetworkToken> buffer;
	private Thread runningThread;
	private boolean isDown = false;
	private final Map<UID, Connection> connectionMap;
	
	public Connection(String adress, int port, OutputBuffer<NetworkToken> buffer, Map<UID, Connection> connectionMap) throws UnknownHostException, IOException {
		this.socket = new Socket();
		socket.connect(new InetSocketAddress(adress, port), 1000);
		this.uid = new UID();
		this.buffer = buffer;
		this.connectionMap = connectionMap;
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		} catch (IOException e) {
			stop();
		}
	}
	
	public UID getConnectionUID(){
		return getUid();
	}
	
	@Override
	public void run() {
		while (!isStopped ){
			synchronized(this){
	            this.runningThread = Thread.currentThread();
	        }
			try {
				String line = "";
				int sign = 0;
				while (((sign = input.read()) != -1) && !isStopped) {
				    line+= (char) sign;
				    if (line.length() >= 255){
				    	System.out.println("ERROR Server sendet zu viele Daten, disconnect");
				    	ClientController.disconnectCurrentConnection();
				    }else if((char) sign == '\n'){
				    	buffer.addMessageIntoOutput(new NetworkToken(line, uid, getIP()));
				    	line = "";
				    }
			    	
				}
				
				//input = clientSocket.getInputStream();
				//System.out.println(input.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} finally {
//				stop();
				ClientController.disconnectCurrentConnection();
				connectionMap.remove(uid);
				isDown  = true;
				System.out.println("Disconnected from Server: " + socket.getRemoteSocketAddress().toString());
			}
		}
		
	}

	public void stop() {
		isStopped = true;
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public String getIP() {
		return socket.getInetAddress().toString();
	}

	public UID getUid() {
		return uid;
	}
	public synchronized void sendMessage(String message){
		OutputStream output;
		message += "\n";
		try {
			output = socket.getOutputStream();
			output.write(message.getBytes("UTF-8"));
			//output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
         
	}
	
}
