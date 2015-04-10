package server.connectionMananger;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import server.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ConnectionManager {
	
	private final InputBuffer<NetworkToken> buffer;
	private final ExecutorService executor; // ThreadPool
	private final Map<UID, Server> serverMap;
	private final Map<UID, Client> clientMap;
	

	
	public ConnectionManager(InputBuffer<NetworkToken> buffer, ExecutorService executor) {
		this.buffer = buffer;
		this.executor = executor;
		this.serverMap = new ConcurrentHashMap<>();
		this.clientMap = new ConcurrentHashMap<>();
	}
	/**
	 * 
	 * @param port - Port auf den der Server Reagiert
	 * @return UID der ServerInstanz
	 */
	public UID startServer(int port){
		UID serverId = new UID();
		Server server = new Server(port, executor, clientMap, buffer);
		serverMap.put(
				serverId, 
				server
		);
		executor.execute(server);
		return serverId;
	}
	
	
	public void stopServer(UID serverId){
		serverMap.get(serverId).stop();
		serverMap.remove(serverId);
	}
	
	public void stopAllServer(){
		ArrayList<UID> toDelete = new ArrayList<>();
		for (Entry<UID, Server> entry: serverMap.entrySet()){
			entry.getValue().stop();
			toDelete.add(entry.getKey());
		}
		for (UID serverId: toDelete){
			serverMap.remove(serverId);
		}
	}
}
