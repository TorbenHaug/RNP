package server.controller;

import java.rmi.server.UID;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.adt.NetworkToken;
import server.awk.AWK;
import server.connectionMananger.ConnectionManager;
import utils.buffer.BufferImpl;

public class Controller {
	private static final BufferImpl<NetworkToken> buffer;
	private static final ExecutorService executor;
	private static final AWK awk;
	private static final ConnectionManager manager;
	private static UID serverID;
	private static String pwd;
	private static long lastUse;
	
	static{
		buffer= new BufferImpl<NetworkToken>();
		executor = Executors.newCachedThreadPool();
		awk = new AWK(buffer);
		manager = new ConnectionManager(buffer, executor);
		lastUse =Calendar.getInstance().getTime().getTime();
	}
	public static void main(String[] args) {
		int port = 8070;
		pwd = "admin";
		executor.execute(awk);
		
		serverID = manager.startServer(8071);
	}
	
	public static boolean shutdown(String pwd){
		if (Controller.pwd.equals(pwd)){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						manager.stopAllServer();
						long timeRemaining = 0;
						while((timeRemaining  = 30 * 1000 - (Calendar.getInstance().getTime().getTime() - lastUse )) > 0){
							Thread.sleep(timeRemaining);
						}
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}).start();
			return true;
		}else{
			return false;
		}
	}
	
	public static synchronized void setUsed(){
		lastUse =Calendar.getInstance().getTime().getTime();
	}
	

}
