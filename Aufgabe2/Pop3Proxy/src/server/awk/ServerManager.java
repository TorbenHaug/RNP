package server.awk;

import java.rmi.server.UID;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import pop3.proxy.client.StopListener;
import pop3.proxy.configReader.Config;
import server.connectionMananger.ServerConnectionManager;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import utils.buffer.InputBuffer;

public class ServerManager {
	private final ServerConnectionManager manager;
	private final MessageDispatcher dispatcher;
	private final Buffer<NetworkToken> buffer;
	private final Map<UID, ClientConnection> connections;
	private final StopListener clientDisconnector;
	
	public ServerManager(ExecutorService executor, int maxLineSize, int port, Set<Config> configs,int timeOut) {
		connections = new ConcurrentHashMap<UID, ClientConnection>();
		buffer = new BufferImpl<NetworkToken>();
		manager = new ServerConnectionManager(buffer, executor, maxLineSize);
		clientDisconnector = new StopListener() {
			
			@Override
			public void stop(UID connectionID) {
				buffer.addMessageIntoOutput(new NetworkToken("+OK QUIT BYE", connectionID, "0.0.0.0"));
				connections.remove(connectionID);
			}
		};
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
				});
				executor.execute(conn);
				connections.put(token.getID(), conn);
				return true;
			}
		});
		executor.execute(dispatcher);
		manager.startServer(port);
	}
	
	
	public void stop(){
		manager.stop();
		dispatcher.stop();
		buffer.stop();
		
	}
}
