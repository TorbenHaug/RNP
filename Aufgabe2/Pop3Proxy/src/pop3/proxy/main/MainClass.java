package pop3.proxy.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pop3.proxy.client.ClientManager;
import pop3.proxy.configReader.Config;
import pop3.proxy.configReader.ConfigReader;
import server.awk.ServerManager;


public class MainClass {
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	private static final String currentDir = System.getProperty("user.dir");
	private static final String dataFolder = currentDir + File.separator + ".." + File.separator + "doc" + File.separator;
	private static final String mailDrop = dataFolder + "mailDrop" + File.separator;
	
	public static void main(String[] args) throws IOException {
		if(Files.notExists(Paths.get(dataFolder))){
			throw new IOException("Unable to access Datafolder: " + dataFolder);
		}
		if(Files.notExists(Paths.get(mailDrop))){
			Files.createDirectory(Paths.get(mailDrop));
		}
		Set<Config> configs = ConfigReader.getFileInput(dataFolder);
		new ClientManager(executor, configs,5,512);
		new ServerManager(executor, 512, 8070, configs, 60, mailDrop);

	}
}
