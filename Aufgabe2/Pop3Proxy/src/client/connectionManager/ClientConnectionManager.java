package client.connectionManager;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import pop3.proxy.client.StopListener;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.OutputBuffer;

public class ClientConnectionManager {
	
	private final Buffer<NetworkToken> buffer;
	private final ExecutorService executor;
	private final Map<UID, Connection> connectionMap;
	private final ClientAnswerHandler answerHandler;
	private final int timeOut;
	private final int maxLineSize;
	private final StopListener listener;

	public ClientConnectionManager(Buffer<NetworkToken> buffer, ExecutorService executor, int timeOut, int maxLineSize) {
		this.timeOut = timeOut*1000;
		this.maxLineSize = maxLineSize;
		this.buffer = buffer;
		this.executor = executor;
		this.connectionMap = new ConcurrentHashMap<>();
		this.answerHandler = new ClientAnswerHandler(buffer, connectionMap);
		executor.execute(answerHandler);
		listener = new StopListener() {
			
			@Override
			public void stop(UID connectionID) {
				stopConnection(connectionID);
				connectionMap.remove(connectionID);
				
			}
		};
	}
	public UID connect(String adress, int port) throws UnknownHostException, IOException{
		Connection connection = new Connection(adress, port, buffer, connectionMap, timeOut, maxLineSize, listener);
		executor.execute(connection);
		connectionMap.put(connection.getUid(), connection);
		return connection.getUid();
	}
	public synchronized void stopConnection(UID uid){
		Connection connection = connectionMap.get(uid);
		if(connection != null)
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
