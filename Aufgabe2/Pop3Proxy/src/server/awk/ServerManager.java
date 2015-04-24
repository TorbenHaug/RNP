package server.awk;

import java.rmi.server.UID;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import pop3.proxy.client.StopListener;
import pop3.proxy.configReader.Configs;
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
	
	public ServerManager(ExecutorService executor, int maxLineSize, int port, Set<Configs> configs) {
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
				connections.put(token.getID(), new ClientConnection(token.getID(), clientDisconnector, buffer));
				return false;
			}
		});
		manager.startServer(port);
	}
	public void stop(){
		manager.stop();
		dispatcher.stop();
		buffer.stop();
		
	}
}
