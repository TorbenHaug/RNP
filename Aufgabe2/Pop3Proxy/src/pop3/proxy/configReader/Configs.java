package pop3.proxy.configReader;

import java.util.List;

public interface Configs {
	
	public String getUser();
	public String getPass();
	public String getServer();
	public int getPort();
	public int getTimeInterval();

}
