package pop3.proxy.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.TimeLimitExceededException;

import pop3.proxy.configReader.Config;
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
	private final String maildrop;
	private final List<Thread> configRunners;
	
	public ClientManager(ExecutorService executor, Set<Config> configs, int connectionTimeOut, int maxLineSize, String mailDrop){
		this.configRunners = new ArrayList<Thread>();
		this.buffer = new BufferImpl<NetworkToken>();
		this.executor = executor;
		this.manager = new ClientConnectionManager(buffer, executor, connectionTimeOut, maxLineSize);
		this.connections = new ConcurrentHashMap<>();
		this.dispatcher = new MessageDispatcher(buffer, connections);
		this.maildrop = mailDrop;
		executor.execute(dispatcher);
		this.listener = new StopListener() {		
			@Override
			public void stop(UID connectionID) {
				manager.stopConnection(connectionID);
				connections.remove(connectionID);
			}
		};
		
		for(Config config: configs){
				Thread newThread = new Thread() {
				
					@Override
					public void run() {
						while(! isInterrupted()){
							UID connectionID;
							try {
								connectionID = manager.connect(config.getServer(), config.getPort());
								ClientConnection connection = new ClientConnection(connectionID, config, listener,buffer, maildrop);
								connections.put(connectionID, connection);
								long deltaLastExecution;
								try {
									//TimeOut Handling Server sollte innerhalb von 5 sec antworten
									while(connections.containsKey(connectionID) && 
											(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution())) < 30000){
										sleep(30000 - deltaLastExecution);
									}
									if(connections.containsKey(connectionID)){
										System.out.println("Connection " + config.getUser() + "doesn't answer.");
										manager.stopConnection(connectionID);
										connections.remove(connectionID);
									}else{
										//Wait period
										while ((config.getTimeInterval()*1000) > 
												(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution()))) {
											sleep((config.getTimeInterval()*1000) - deltaLastExecution);							
										}
									}
								} catch (InterruptedException e) {
									
								}
							} catch (Exception e) {
								System.out.println("Connection Errror: " + config.getUser() + " retry in 5 sec.");
								try {
									sleep(5000);
								} catch (InterruptedException e1) {
									
								}
							}
							
						}
						
					}
				};
			configRunners.add(newThread);
			executor.execute(newThread);
			
		}
	}
}
