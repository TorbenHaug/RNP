package pop3.proxy.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pop3.proxy.client.ClientManager;
import pop3.proxy.configReader.Config;
import pop3.proxy.configReader.ConfigReader;
import server.awk.ServerManager;


public class MainClass {
	private static ClientManager clientManager;
	private static ServerManager serverManager;
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	private static final String currentDir = System.getProperty("user.dir");
	private static final String dataFolder = currentDir + File.separator + ".." + File.separator + "doc" + File.separator;
	private static final String mailDrop = dataFolder + "mailDrop" + File.separator;
	
	static class ShutdownThread extends Thread {
		  	  
		  public ShutdownThread() {
		    super();
		  }
		   
		  public void run() {
		    System.out.println("[Shutdown thread] Shutting down");
		    clientManager.stop();
		    serverManager.stop();
		    while(!executor.isTerminated()) {
				try {
					executor.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Executor: " + executor.isTerminated());
		    System.out.println("[Shutdown thread] Shutdown complete");
		  }
		}
	
	public static void main(String[] args) throws IOException {
		try {
	      Runtime.getRuntime().addShutdownHook(new ShutdownThread());
	      System.out.println("[Main thread] Shutdown hook added");
	    } catch (Throwable t) {
	      // we get here when the program is run with java
	      // version 1.2.2 or older
	      System.out.println("[Main thread] Could not add Shutdown hook");
	    }
		if(Files.notExists(Paths.get(dataFolder))){
			throw new IOException("Unable to access Datafolder: " + dataFolder);
		}
		if(Files.notExists(Paths.get(mailDrop))){
			Files.createDirectory(Paths.get(mailDrop));
		}
		Set<Config> configs = ConfigReader.getFileInput(dataFolder);
		clientManager = new ClientManager(executor, configs,5,512, mailDrop,1);
		serverManager = new ServerManager(executor, 512, 8070, configs, 30, mailDrop);

	}
}
