package server.connectionMananger;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class ServerConnectionManager {
	
	private final InputBuffer<NetworkToken> buffer;
	private final ExecutorService executor; // ThreadPool
	private final ServerAnswerHandler answerHandler;
	private final Map<UID, Server> serverMap;
	private final Map<UID, ClientConnectionDokument> clientMap;
	private final int maxLineSize;
	private final int maxIncomingConnections;


	public ServerConnectionManager(InputBuffer<NetworkToken> buffer, ExecutorService executor, int maxLineSize, int maxIncomingConnections) {
		this.maxIncomingConnections = maxIncomingConnections;
		this.maxLineSize = maxLineSize;
		this.buffer = buffer;
		this.executor = executor;
		this.serverMap = new ConcurrentHashMap<>();
		this.clientMap = new ConcurrentHashMap<>();
		this.answerHandler = new ServerAnswerHandler(buffer, clientMap);
		executor.execute(answerHandler);
	}
	/**
	 * 
	 * @param port - Port auf den der Server Reagiert
	 * @return UID der ServerInstanz
	 */
	public UID startServer(int port){
		UID serverId = new UID();
		Server server = new Server(port, executor, clientMap, buffer, maxLineSize, maxIncomingConnections);
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
	public void stop(){
		Collection<ClientConnectionDokument> clients = clientMap.values();
		for(ClientConnectionDokument client: clients){
			client.stop();
		}
		answerHandler.stop();

	}
	public void stopClient(UID connectionID) {
		clientMap.get(connectionID).stop();
		
	}
}
