package pop3.proxy.main;

import java.util.HashSet;
import java.util.concurrent.Executors;

import pop3.proxy.client.ClientManager;
import pop3.proxy.configReader.Configs;

public class MainClass {
	public static void main(String[] args) {
		HashSet<Configs> configs = new HashSet<Configs>();
		configs.add(new Configs() {
			
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
				return "141.22.72.69";
			}
			
			@Override
			public int getPort() {
				return 110;
			}
			
			@Override
			public String getPass() {
				return "rnp";
			}
		});
		new ClientManager(Executors.newCachedThreadPool(), configs);
	}
}
