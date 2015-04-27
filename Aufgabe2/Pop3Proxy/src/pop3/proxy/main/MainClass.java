package pop3.proxy.main;

import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pop3.proxy.client.ClientManager;
import pop3.proxy.configReader.Config;
import server.awk.ServerManager;
import server.connectionMananger.Server;
import server.connectionMananger.ServerConnectionManager;
import utils.adt.NetworkToken;
import utils.buffer.BufferImpl;

public class MainClass {
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	public static void main(String[] args) {
		HashSet<Config> config = new HashSet<Config>();
		config.add(new Config() {
			
			@Override
			public String getUser() {
				return "rnp";
			}
			
			@Override
			public int getTimeInterval() {
				return 30;
			}
			
			@Override
			public String getServer() {
				return "127.0.0.1";
			}
			
			@Override
			public int getPort() {
				return 8073;
			}
			
			@Override
			public String getPass() {
				return "rnp";
			}
		});
		new ClientManager(executor, config,5,512);
		new ServerManager(executor, 512, 8070, config, 60);
	}
}
