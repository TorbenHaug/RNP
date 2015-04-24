package pop3.proxy.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pop3.proxy.configReader.Configs;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import utils.buffer.InputBuffer;
import client.connectionManager.ClientConnectionManager;

public class ClientManager {
	private final ClientConnectionManager manager;
	private final Buffer<NetworkToken> buffer;
	private final ExecutorService executor;
	private final Map<UID, ClientConnection> connections;
	private final StopListener listener;
	private final MessageDispatcher dispatcher;
	
	public ClientManager(ExecutorService executor, Set<Configs> configs){
		this.buffer = new BufferImpl<NetworkToken>();
		this.executor = executor;
		this.manager = new ClientConnectionManager(buffer, executor);
		this.connections = new ConcurrentHashMap<>();
		dispatcher = new MessageDispatcher(buffer, connections);
		executor.execute(dispatcher);
		this.listener = new StopListener() {		
			@Override
			public void stop(UID connectionID) {
				manager.stopConnection(connectionID);
				connections.remove(connectionID);
			}
		};
		
		for(Configs config: configs){
			UID connectionID;
			try {
				connectionID = manager.connect(config.getServer(), config.getPort());
				ClientConnection connection = new ClientConnection(connectionID, config, listener,buffer);
				connections.put(connectionID, connection);
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			
		}
	}
}
