package server.awk;

import pop3.proxy.configReader.Config;

public interface CheckUser {
	public boolean userExists(String userName);
	public Config checkPass(String userName, String pass);
}
