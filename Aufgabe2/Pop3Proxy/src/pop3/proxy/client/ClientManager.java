package pop3.proxy.client;

import java.io.IOException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import pop3.proxy.configReader.AccountConfig;
import utils.adt.NetworkToken;
import utils.buffer.Buffer;
import utils.buffer.BufferImpl;
import client.connectionManager.ClientConnectionManager;

public class ClientManager {
	private final ClientConnectionManager manager;
	private final Buffer<NetworkToken> buffer;
	private final ExecutorService executor;
	private final Map<UID, ClientConnection> connections;

	private final MessageDispatcher dispatcher;
	private final String maildrop;
	private final List<Stopable> configRunners;
	private final Semaphore runningMailClients;
	private final ExecutorService clientExecuter;
	private final int connectionTout;
	private final int maxMailSize;

	public ClientManager(ExecutorService executor, Set<AccountConfig> accountConfigs, int connectionTimeOut, int maxLineSize, String mailDrop, int maxConnections, int maxMailSize){
		this.maxMailSize = maxMailSize;
		this.connectionTout = connectionTimeOut * 1000;
		this.clientExecuter = Executors.newCachedThreadPool();
		this.runningMailClients = new Semaphore(maxConnections, true);
		this.configRunners = new ArrayList<Stopable>();
		this.buffer = new BufferImpl<NetworkToken>();
		this.executor = executor;
		this.manager = new ClientConnectionManager(buffer, executor, maxLineSize);
		this.connections = new ConcurrentHashMap<>();
		this.dispatcher = new MessageDispatcher(buffer, connections);
		this.maildrop = mailDrop;
		executor.execute(dispatcher);

		
		for(AccountConfig accountConfig : accountConfigs){
				Stopable newThread = new Stopable() {
					public boolean isStopped = false;
					Thread runningThread;
					private StopListener listener;
					@Override
					public void run() {
						this.listener = new StopListener() {
						@Override
							public void stop(UID connectionID) {

								manager.stopConnection(connectionID);
								connections.remove(connectionID);
								runningThread.interrupt();
							}
						};
						synchronized(this){
							this.runningThread = Thread.currentThread();
						}

						while(! isStopped){
							UID connectionID;
							try {
								runningMailClients.acquire();
								connectionID = manager.connect(accountConfig.getServer(), accountConfig.getPort(), listener);
								ClientConnection connection = new ClientConnection(connectionID, accountConfig, listener,buffer, maildrop, maxMailSize);
								connections.put(connectionID, connection);
								long deltaLastExecution;

								//TimeOut Handling Server sollte innerhalb von 5 sec antworten
								System.out.println("Receiving Mails for " + accountConfig.getUser());
								while(connections.containsKey(connectionID) &&
										(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution())) < connectionTout){
									try {
										Thread.sleep(connectionTout - deltaLastExecution);
									}catch (InterruptedException e){
										if(isStopped){
											//TimeOut Handling Server sollte innerhalb von 5 sec antworten
											while(connections.containsKey(connectionID) &&
													(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution())) < connectionTout){
												try {
													Thread.sleep(connectionTout - deltaLastExecution);
												}catch (InterruptedException e1){

												}

											}
											manager.stopConnection(connectionID);
											connections.remove(connectionID);
											System.out.println("Release " + accountConfig.getUser());
											runningMailClients.release();
											Thread.currentThread().interrupt();
										}
									}

								}
								if(connections.containsKey(connectionID)){
									System.out.println("Connection " + accountConfig.getUser() + " doesn't answer.");
									manager.stopConnection(connectionID);
									connections.remove(connectionID);
									System.out.println("Release " + accountConfig.getUser());
									runningMailClients.release();
								}else{
									//Wait period
									System.out.println("Release " + accountConfig.getUser());
									runningMailClients.release();
									System.out.println("Connection " + accountConfig.getUser() + " is waiting " + accountConfig.getTimeInterval() + " sec");
									while (!isStopped && (accountConfig.getTimeInterval()*1000) >
											(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution()))) {
										try {
											Thread.sleep((accountConfig.getTimeInterval() * 1000) - deltaLastExecution);
										}catch(InterruptedException e){
											if(isStopped){
												//TimeOut Handling Server sollte innerhalb von 5 sec antworten
												while(connections.containsKey(connectionID) &&
														(deltaLastExecution = (System.currentTimeMillis() - connection.getLastExecution())) < connectionTimeOut) {
													try {
														Thread.sleep(connectionTimeOut - deltaLastExecution);
													} catch (InterruptedException e1) {

													}

												}
												Thread.currentThread().interrupt();
											}
										}
									}
								}
							} catch (IOException e) {
								System.out.println("Connection Errror: " + accountConfig.getUser() + " retry in 5 sec.");
								try {
									runningMailClients.release();
									Thread.sleep(5000);
								} catch (InterruptedException e1) {
									Thread.currentThread().interrupt();
								}
								//System.out.println("Reconnect " + config.getUser() );
							}catch (InterruptedException e){
								//System.out.println(e);
							}
							
						}
						System.out.println(accountConfig.getUser() + " herruntergefahren");
						Thread.currentThread().interrupt();
					}

					@Override
					public void stop() {
						isStopped = true;
						this.runningThread.interrupt();
						
					}
				};
			configRunners.add(newThread);
			clientExecuter.execute(new Thread(newThread));
			
		}
	}

	public void stop() {
		clientExecuter.shutdown();
		for(Stopable thread: configRunners){
			thread.stop();
			//System.out.println(thread.interrupted());
		}
		while (!clientExecuter.isTerminated()) {
			System.out.println("Waiting for receiving Mailaccounts");
			try {
				clientExecuter.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All Message receivers has been shutdown");
		manager.stop();
		dispatcher.stop();
		buffer.stop();
		System.out.println("ClientManager herruntergefahren");
	}
}
