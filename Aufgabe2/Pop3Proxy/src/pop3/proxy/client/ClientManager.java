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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

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
	private final List<Stopable> configRunners;
	private final Semaphore runningMailClients;
	
	public ClientManager(ExecutorService executor, Set<Config> configs, int connectionTimeOut, int maxLineSize, String mailDrop, int maxConnections){
		this.runningMailClients = new Semaphore(maxConnections, true);
		this.configRunners = new ArrayList<Stopable>();
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
				Stopable newThread = new Stopable() {
					public boolean isStopped = false;
					Thread runningThread;
					@Override
					public void run() {

						while(! isStopped){
							UID connectionID;
							try {
								synchronized(this){
									this.runningThread = Thread.currentThread();
								}
								runningMailClients.acquire();
								connectionID = manager.connect(config.getServer(), config.getPort());
								ClientConnection connection = new ClientConnection(connectionID, config, listener,buffer, maildrop);
								connections.put(connectionID, connection);
								long deltaLastExecution;
								try {
									//TimeOut Handling Server sollte innerhalb von 5 sec antworten
									while(connections.containsKey(connectionID) && 
											(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution())) < 5000){
										Thread.sleep(5000 - deltaLastExecution);
										
									}
									if(connections.containsKey(connectionID)){
										System.out.println("Connection " + config.getUser() + "doesn't answer.");
										manager.stopConnection(connectionID);
										connections.remove(connectionID);
										runningMailClients.release();
									}else{
										//Wait period
										while ((config.getTimeInterval()*1000) > 
												(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution()))) {
											Thread.sleep((config.getTimeInterval()*1000) - deltaLastExecution);							
										}
									}
								} catch (InterruptedException e) {
									manager.stopConnection(connectionID);
									connections.remove(connectionID);
									runningMailClients.release();
									Thread.currentThread().interrupt();
								}
							} catch (Exception e) {
								System.out.println("Connection Errror: " + config.getUser() + " retry in 5 sec.");
								try {
									runningMailClients.release();
									Thread.sleep(5000);
								} catch (InterruptedException e1) {
									Thread.currentThread().interrupt();
								}
							}
							
						}
						System.out.println(config.getUser() + " herruntergefahren");
						Thread.currentThread().interrupt();
					}

					@Override
					public void stop() {
						isStopped = true;
						this.runningThread.interrupt();
						
					}
				};
			configRunners.add(newThread);
			executor.execute(new Thread(newThread));
			
		}
	}

	public void stop() {
		executor.shutdown();
		buffer.stop();
		manager.stopAllConnections();
		for(Stopable thread: configRunners){
			thread.stop();
			//System.out.println(thread.interrupted());
		}
		dispatcher.stop();
		System.out.println("ClientManager herruntergefahren");
	}
}
