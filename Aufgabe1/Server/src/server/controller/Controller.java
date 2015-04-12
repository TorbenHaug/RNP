package server.controller;

import java.rmi.server.UID;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.adt.NetworkToken;
import server.awk.AWK;
import server.connectionMananger.AnswerHandler;
import server.connectionMananger.ServerConnectionManager;
import utils.buffer.BufferImpl;

public class Controller {
	private static final BufferImpl<NetworkToken> buffer;
	private static final ExecutorService executor;
	private static final AWK awk;
	private static final ServerConnectionManager manager;
	private static UID serverID;
	private static String pwd;
	private static long lastUse;
	
	static{
		buffer= new BufferImpl<NetworkToken>();
		executor = Executors.newCachedThreadPool();
		awk = new AWK(buffer);
		manager = new ServerConnectionManager(buffer, executor);
		lastUse =Calendar.getInstance().getTime().getTime();
	}
	public static void main(String[] args) {
		int port = 8070;
		pwd = "admin";
		for(int i = 0; i < args.length; i++){
			if((i + 1) < args.length) {							//i+1 is not allowed to be the length of array --> ArrayOutOfBounce
				if(args[i].equals("-port")){					// if user gives port
					String nextArgument = args[++i];
					if(!nextArgument.equals("-pwd")){
						port = Integer.valueOf(nextArgument);
					}
				}else if(args[i].equals("-pwd")){				//if user gives password
					pwd = args[++i];
				}
				else{
					System.out.println("ERROR unknown command");
					return;
				}
			}else{
				System.out.println("Port: " + port + " Pwd: " + pwd);  // if neither port and password nor one of them are given
			}
		}
		
		executor.execute(awk);
		
		serverID = manager.startServer(port);
	}
	
	public static boolean shutdown(String pwd){
		if ((Controller.pwd + "\n").equals(pwd)){
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						manager.stopAllServer();
						long timeRemaining = 0;
						while((timeRemaining  = 30 * 1000 - (Calendar.getInstance().getTime().getTime() - lastUse )) > 0){
							System.out.println(timeRemaining);
							Thread.sleep(timeRemaining);
						}
						awk.stop();
						manager.stop();
						buffer.stop();
						executor.shutdown();
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
			return true;
		}else{
			return false;
		}
	}
	
	public static synchronized void setUsed(){
		lastUse =Calendar.getInstance().getTime().getTime();
	}
	

}
