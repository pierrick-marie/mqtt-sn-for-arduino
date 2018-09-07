package utils;

import java.util.Map;

public enum ClientsManager {

	Instance;

	private static final ClientList clients = new ClientList();

	public Client newClient(final byte[] address64) {

		Client client = new Client();

		client.setAddress64(address64);

		return clients.put(client.getStringAddress64(), client);
	}

	public Client getClientByAddress(final Client client) {

		return getClient(client.address64());
	}

	public Client getClientByName(final Client client) {

		return getClient(client.name());
	}

	public Client getClient(final String clientName) {

		Client client = null;

		for (Map.Entry<String, Client> entry : clients.entrySet()) {
			if(entry.getValue().name().equals(clientName)) {
				client = entry.getValue() ;
				break;
			}
		}

		if (null == client) {
			Log.error("ClientsManager", "getClientByName", "client " + clientName + " is null");
		}

		return client;
	}

	public Client getClient(final byte[] address64) {

		Client client = clients.get(Client.addressToString(address64));

		if (null == client) {
			Log.error("ClientsManager", "getClientByAddress", "client " + address64 + " is null");
		}

		return client;
	}
}

