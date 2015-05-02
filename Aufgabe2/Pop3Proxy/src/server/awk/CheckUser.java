package server.awk;

import pop3.proxy.configReader.AccountConfig;

public interface CheckUser {
	
	public boolean userExists(String userName);
	public AccountConfig checkPass(String userName, String pass);
	
}
