package utils;

import java.util.HashMap;

public class ClientList extends HashMap<String, Client> {

	protected ClientList() { }

	public Client put(final Client client) {
		return put(client.name(), client);
	}
}
