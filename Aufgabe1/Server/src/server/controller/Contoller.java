package server.controller;

import java.util.concurrent.Executors;

import server.adt.NetworkTocken;
import server.connectionMananger.ConnectionManager;
import utils.buffer.BufferImpl;

public class Contoller {

	public static void main(String[] args) {
		
		ConnectionManager manager = new ConnectionManager(new BufferImpl<NetworkTocken>(), Executors.newCachedThreadPool());
		manager.startServer(8070);

	}

}
