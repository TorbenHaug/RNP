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
import client.connectionManager.ConnectionManager;

public class Controller {
	private final static ConnectionManager manager;
	private static final Buffer<NetworkToken> buffer;
	private static final ExecutorService executor;
	static{
		buffer = new BufferImpl<>();
		executor = Executors.newCachedThreadPool();
		manager = new ConnectionManager(buffer, executor);
	}
	public static void main(String[] args) {
		
	}
	public static void shutdown(){
		buffer.stop();
		manager.stopAllConnections();
	}
	public static UID connect(String adress, int port) throws UnknownHostException, IOException{
		return manager.connect(adress, port);
	}
}
