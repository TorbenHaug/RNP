package pop3.proxy.client;

public enum ClientState {

	Connected,
	User,
	Pass,
	Transaction,
	List,
	Reading,
	Delete,
	Update
}
