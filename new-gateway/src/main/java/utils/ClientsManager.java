package utils;

import utils.address.Address64;

import java.util.Map;

enum ClientsManager {

	Instance;

	private final ClientList clients = new ClientList();

	Client search(final Address64 address) {

		Client client = clients.get(address);

		if (null == client) {
			Log.error("ClientsManager", "get", "client with address " + address + " is null");
		}

		return client;
	}

	Client save(final Client client) {

		if(null == client || null == client.address64) {
			Log.error("ClientList", "put", "client = " + client + " & address64 = " + client.address64);
			return null;
		}

		if(null == clients.put(client.address64, client)){
			Log.debug("ClientList", "put", "client " + client + " is not already registered");
		}

		return client;
	}
}

