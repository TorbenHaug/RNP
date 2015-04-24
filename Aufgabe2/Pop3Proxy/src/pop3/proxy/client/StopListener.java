package pop3.proxy.client;

import java.rmi.server.UID;


public interface StopListener {
	public void stop(UID connectionID);
}
