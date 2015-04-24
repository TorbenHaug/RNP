package pop3.proxy.configReader;

<<<<<<< HEAD
=======
import java.util.List;
//import org.json.simple.parser.JSONParser;
>>>>>>> 51c20ed6a38b713b5d0aa6be68706d5afe6c68ab


class ConfigsImpl implements Configs{
	
	String user;
	String pass;
	String server;
	int port;
	int timeInterval;
	

	ConfigsImpl(String user, String pass, String server, int port, int timeInterval){
		this.user = user;
		this.pass = pass;
		this.server = server;
		this.port = port;
		this.timeInterval = timeInterval;
	}

	
	@Override
	public String getUser() {
		return this.user;
	}

	@Override
	public String getPass() {
		return this.pass;
	}

	@Override
	public String getServer() {
		return this.server;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public int getTimeInterval() {
		return this.timeInterval;
	}

	

	
}
