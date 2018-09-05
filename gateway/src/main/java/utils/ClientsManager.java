package utils;

public enum ClientsManager {

	Instance;

	private static final ClientList clients = new ClientList();

	public Client newClient(final String clientName) {

		Client client = new Client();

		client.setName(clientName);
		clients.put(client);

		return client;
	}

	public Client getClient(final String clientName) {

		Client client = clients.get(clientName);

		if (null == client) {
			Log.error("ClientsManager", "getClient", "Client " + clientName + " is null");
		}

		return client;
	}
}

