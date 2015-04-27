package pop3.proxy.client;

public enum ClientState {

	Connected,
	User,
	Pass,
	Transaction,
	Reading,
	Delete,
	Update
}
