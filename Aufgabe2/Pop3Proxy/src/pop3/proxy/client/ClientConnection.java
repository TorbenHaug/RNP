package pop3.proxy.client;

import pop3.proxy.configReader.Configs;

public class ClientConnection implements Runnable{

	private Configs config;
	
	public ClientConnection(Configs config) {
		this.config = config;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
