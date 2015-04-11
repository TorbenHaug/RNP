package client.connectionManager;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;
import utils.buffer.OutputBuffer;

public class ClientConnectionManager {
	
	private OutputBuffer<NetworkToken> buffer;
	private ExecutorService executor;
	private Map<UID, Connection> connectionMap;
	private ClientAnswerHandler answerHandler;

	public ClientConnectionManager(OutputBuffer<NetworkToken> buffer, ExecutorService executor) {
		this.buffer = buffer;
		this.executor = executor;
		this.connectionMap = new ConcurrentHashMap<>();
		this.answerHandler = new ClientAnswerHandler(buffer, connectionMap);
		executor.execute(answerHandler);
	}
	public UID connect(String adress, int port) throws UnknownHostException, IOException{
		Connection connection = new Connection(adress, port, buffer, connectionMap);
		executor.execute(connection);
		connectionMap.put(connection.getUid(), connection);
		return connection.getUid();
	}
	public void stopConnection(UID uid){
		Connection connection = connectionMap.get(uid);
		connection.stop();
	}
	public void stopAllConnections(){
		Collection<UID> uids = connectionMap.keySet();
		for(UID uid: uids){
			stopConnection(uid);
		}
	}
	public void stop(){
		stopAllConnections();
		answerHandler.stop();
	}
}
