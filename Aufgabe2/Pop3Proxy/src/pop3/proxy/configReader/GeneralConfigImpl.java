package pop3.proxy.configReader;

public class GeneralConfigImpl implements GeneralConfig {

	private int serverPort; 
	private int maxServerConnection; 
	private int serverTimeout; 
	private int maxSignsPerLineServer; 
	
	private int maxClientConnections; 
	private int clientTimeout; 
	private int maxSignsPerLineClient; 
	private int maxMailSize;
	
	public GeneralConfigImpl(int serverPort, int maxServerConnection, int serverTimeout, int maxSignsPerLineServer, 
			int maxClientConnections, int clientTimeout, int maxSignsPerLineClient, int maxMailSize){
		
		 this.serverPort = serverPort;
		 this.maxServerConnection = maxServerConnection;
		 this.serverTimeout = serverTimeout; 
		 this.maxSignsPerLineServer = maxSignsPerLineClient;
		 
		 this.maxClientConnections = maxClientConnections; 
		 this.clientTimeout = clientTimeout; 
		 this.maxSignsPerLineClient = maxSignsPerLineClient; 
		 this.maxMailSize = maxMailSize * 1024 * 1024;
		
	}
	
	@Override
	public int getServerport() {
		return serverPort;
	}

	@Override
	public int getMaxServerConnections() {
		return maxServerConnection;
	}

	@Override
	public int getServerTimeout() {
		return serverTimeout;
	}

	@Override
	public int getMaxSignsPerLineServer() {
		return maxSignsPerLineServer;
	}

	@Override
	public int getMaxClientConnections() {
		return maxClientConnections;
	}

	@Override
	public int getClientTimeout() {
		return clientTimeout;
	}

	@Override
	public int getMaxSignsPerLineClient() {
		return maxSignsPerLineClient;
	}

	@Override
	public int getMaxMailSize() {
		return maxMailSize;
	}

}
