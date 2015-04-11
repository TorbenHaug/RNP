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
			executor.shutdown();
			manager.stop();
			if (gui != null)
				gui.stop();
			buffer.stop();
		}
	}
	public static UID connect(String adress, int port) throws UnknownHostException, IOException{
		return manager.connect(adress, port);
	}
}
