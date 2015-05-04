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
import pop3.proxy.configReader.AccountConfig;
import pop3.proxy.configReader.ConfigReader;
import pop3.proxy.configReader.GeneralConfig;
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
			executor.shutdown();
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
//		GeneralConfig generalConfig = new GeneralConfig() {
//			@Override
//			public int getServerport() {
//				return 8070;
//			}
//
//			@Override
//			public int getMaxServerConnections() {
//				return 3;
//			}
//
//			@Override
//			public int getServerTimeout() {
//				return 30;
//			}
//
//			@Override
//			public int getMaxSignsPerLineServer() {
//				return 70;
//			}
//
//			@Override
//			public int getMaxClientConnections() {
//				return 1;
//			}
//
//			@Override
//			public int getClientTimeout() {
//				return 30;
//			}
//
//			@Override
//			public int getMaxSignsPerLineClient() {
//				return 512;
//			}
//
//			@Override
//			public int getMaxMailSize() {
//				return 5*1024*1024;
//			}
//		};
		Set<AccountConfig> accountConfigs = ConfigReader.getFileInput(dataFolder);
		GeneralConfig generalConfig = ConfigReader.getFileInputForGeneral(dataFolder);
		clientManager = new ClientManager(executor, accountConfigs,generalConfig.getClientTimeout(),generalConfig.getMaxSignsPerLineClient(), mailDrop,generalConfig.getMaxClientConnections(), generalConfig.getMaxMailSize());
		serverManager = new ServerManager(executor, generalConfig.getMaxSignsPerLineServer(), generalConfig.getServerport(), accountConfigs, generalConfig.getServerTimeout(), mailDrop, generalConfig.getMaxServerConnections());

	}
}
