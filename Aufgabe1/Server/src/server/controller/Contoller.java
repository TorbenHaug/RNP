package server.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.adt.NetworkToken;
import server.awk.AWK;
import server.connectionMananger.ConnectionManager;
import utils.buffer.BufferImpl;

public class Contoller {

	public static void main(String[] args) {
		BufferImpl<NetworkToken> buffer= new BufferImpl<NetworkToken>();
		ExecutorService executor = Executors.newCachedThreadPool();
		AWK awk = new AWK(buffer);
		executor.execute(awk);
		ConnectionManager manager = new ConnectionManager(buffer, executor);
		manager.startServer(8071);
		

	}

}
