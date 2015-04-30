package server.awk;

import java.rmi.server.UID;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pop3.proxy.client.StopListener;
import pop3.proxy.configReader.Config;
import server.connectionMananger.ServerConnectionManager;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import utils.buffer.InputBuffer;

public class ServerManager {
	private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
	private final ServerConnectionManager manager;
	private final MessageDispatcher dispatcher;
	private final Buffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	private final StopListener clientDisconnector;
	private final Map<UID,String> lookedUsers;
	private final String mailDrop;
	
	public ServerManager(ExecutorService executor, int maxLineSize, int port, Set<Config> configs,int timeOut, String dataFolder) {
		this.mailDrop = dataFolder;
		connections = new ConcurrentHashMap<UID, ClientConnection>();
		buffer = new BufferImpl<NetworkToken>();
		manager = new ServerConnectionManager(buffer, executor, maxLineSize);
		clientDisconnector = new StopListener() {
			
			@Override
			public void stop(UID connectionID) {
				buffer.addMessageIntoOutput(new NetworkToken("+OK QUIT BYE", connectionID, "0.0.0.0"));
				lookedUsers.remove(connectionID);
				connections.remove(connectionID);
			}
		};
		lookedUsers = new ConcurrentHashMap<>();
		dispatcher = new MessageDispatcher(buffer, connections,clientDisconnector, new CreateClientConnection() {
			
			@Override
			public boolean createClient(NetworkToken token) {
				ClientConnection conn = new ClientConnection(token.getID(), clientDisconnector, buffer, timeOut, new CheckUser() {
					
					@Override
					public boolean userExists(String userName) {
						for(Config config: configs){
							if(config.getUser().equals(userName)){
								return true;
							}
						}
						return false;
					}
					
					@Override
					public Config checkPass(String userName, String pass) {
						for(Config config: configs){
							if(config.getUser().equals(userName) && config.getPass().equals(pass)){
								return config;
							}
						}
						return null;
					}
				}, lookedUsers, mailDrop);
				clientExecutor.execute(new Thread(conn));
				connections.put(token.getID(), conn);
				return true;
			}
		});
		executor.execute(dispatcher);
		manager.startServer(port);
	}
	
	
	public void stop(){
		manager.stopAllServer();
		System.out.println("ClientsShutdown");
		clientExecutor.shutdown();
		while (!clientExecutor.isTerminated()) {
			System.out.println("Waiting for connected Clients");
			try {
				clientExecutor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All Clients are disconnected");
		manager.stop();
		dispatcher.stop();
		buffer.stop();
		System.out.println("ServerManager herruntergefahren");
	}
}
