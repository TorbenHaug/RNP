package pop3.proxy.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pop3.proxy.configReader.Configs;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import client.connectionManager.ClientConnectionManager;

public class ClientManager {
	private final ClientConnectionManager manager;
	private final Buffer<NetworkToken> buffer;
	private final ExecutorService executor;
	private final Set<ClientConnection> connections;
	
	public ClientManager(ExecutorService executor, Set<Configs> configs){
		this.buffer = new BufferImpl<NetworkToken>();
		this.executor = executor;
		this.manager = new ClientConnectionManager(buffer, executor);
		this.connections = new HashSet<ClientConnection>();
		for(Configs config: configs){
			UUID clientID = manager.connect(config., port)
		}
	}
}
