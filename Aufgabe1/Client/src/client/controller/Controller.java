package client.controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import utils.buffer.OutputBuffer;
import client.connectionManager.ClientConnectionManager;
import client.gui.GuiManager;

public class Controller {
	private final static ClientConnectionManager manager;
	private static final Buffer<NetworkToken> buffer;
	private static final ExecutorService executor;
	private static GuiManager gui;
	private static boolean isShuttingDown;
	private static UID currentConnection = null;
	static{
		buffer = new BufferImpl<>();
		executor = Executors.newCachedThreadPool();
		manager = new ClientConnectionManager(buffer, executor);
		
	}
	public static void main(String[] args) {
		gui = new GuiManager(buffer, executor);
	}
	public static void shutdown(){
		if(!isShuttingDown){
			isShuttingDown = true;
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					
					System.out.println("ConnectionManager Herunterfahren");
					manager.stop();
					if (gui != null){
						System.out.println("GUI Herunterfahren");
						gui.stop();
					}
					System.out.println("Buffer anhalten");
					buffer.stop();
					System.out.println("ThreadPool Herunterfahren");
					executor.shutdown();
				}
			});
		}
	}
	public static UID connect(String adress, int port) throws UnknownHostException, IOException{
		currentConnection = manager.connect(adress, port);
		return currentConnection;
	}
	public static void disconnectCurrentConnection(){
		manager.stopConnection(currentConnection);
		gui.connectionStopped(currentConnection);
	}
}
