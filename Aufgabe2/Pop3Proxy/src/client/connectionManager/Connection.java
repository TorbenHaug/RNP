package client.connectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Map;

import pop3.proxy.client.StopListener;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.OutputBuffer;

public class Connection implements Runnable{
	private Socket socket;
	private UID uid;
	private boolean isStopped = false;
	private BufferedReader input;
	private final Buffer<NetworkToken> buffer;
	private Thread runningThread;
	private boolean isDown = false;
	private final Map<UID, Connection> connectionMap;
	private final int maxLineSize;
	private boolean isQuited = false;
	private final StopListener listener;
	
	public Connection(String adress, int port, Buffer<NetworkToken> buffer, Map<UID, Connection> connectionMap, int timeOut, int maxLineSize, StopListener listener) throws UnknownHostException, IOException {
		this.listener = listener;
		this.maxLineSize = maxLineSize;
		this.socket = new Socket();
		try{
			socket.connect(new InetSocketAddress(adress, port), timeOut);
		}catch(SocketTimeoutException e){
			stop();
		}
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
				    if (line.length() >= maxLineSize){
				    	System.out.println("ERROR Server sendet zu viele Daten, disconnect");
				    	listener.stop(uid);
				    }else if(line.endsWith("\r\n")){
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
				listener.stop(uid);
				//ClientController.disconnectCurrentConnection();
				//TODO: DisconnectHandling
				connectionMap.remove(uid);
				isDown  = true;
				System.out.println("Disconnected from Server: " + socket.getRemoteSocketAddress().toString());
			}
		}
		
	}

	public void stop() {
		isStopped = true;
		if(!isQuited){
			sendMessage("QUIT");
			isQuited = true;
		}
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
		try {
			if(message.startsWith("QUIT")){
				this.isQuited  = true;
			}
			message += "\r\n";
			output = socket.getOutputStream();
			output.write(message.getBytes("UTF-8"));
			//output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
         
	}
	
}
