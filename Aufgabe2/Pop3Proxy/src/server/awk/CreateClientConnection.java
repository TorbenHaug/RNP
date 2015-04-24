package server.awk;

import java.rmi.server.UID;

import utils.adt.NetworkToken;

public interface CreateClientConnection {
	public boolean createClient(NetworkToken token);
}
