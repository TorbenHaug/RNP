package server.connectionMananger;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class Server implements Runnable{

    private int          serverPort   = 8080;
    private ServerSocket serverSocket = null;
    private boolean      isStopped    = false;
    private Thread       runningThread= null;
    private final ExecutorService executor;
    private final Map<UID, ClientConnectionDokument> clientMap;
    private final InputBuffer<NetworkToken> buffer;
	private final int maxLineSize;

    public Server(int port, ExecutorService executor, Map<UID, ClientConnectionDokument> clientMap, InputBuffer<NetworkToken> buffer, int maxLineSize){
        this.maxLineSize = maxLineSize;
    	this.serverPort = port;
        this.executor = executor;
        this.clientMap = clientMap;
        this.buffer = buffer;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                	System.out.println((new Date()).toString() +  " Server "+ serverPort + " stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            if (clientMap.size() >= 3){
            	OutputStream output;
				try {
					output = clientSocket.getOutputStream();
					output.write(("ERROR too many clients connected").getBytes());
	            	output.close();
	            	System.out.println((new Date()).toString() + " Client from " + clientSocket.getInetAddress().getHostAddress() + 
	            			" refused, because ther are too many clients");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						clientSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
					}
				}
            	
            }
            else{
            	ClientConnectionDokument client = new ClientConnectionDokument(clientSocket, buffer,clientMap, maxLineSize);
            	clientMap.put(client.getClientId(), client);
                executor.execute(client);
                System.out.println((new Date()).toString() + " Client " + client.getClientId() + " IP: " +  client.getIP() + " has connected");
            }
        }
        System.out.println((new Date()).toString() +  " Server "+ serverPort + " stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.out.println("Cannot open port " + serverPort);
            System.exit(-1);
        }
        System.out.println((new Date()).toString() + " Server listening to port " + serverPort);
    }

}