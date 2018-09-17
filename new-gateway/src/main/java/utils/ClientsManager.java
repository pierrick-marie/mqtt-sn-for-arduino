package utils;

import utils.address.Address64;

import java.util.Map;

enum ClientsManager {

	Instance;

	private volatile ClientList clients = new ClientList();

	synchronized Client search(final Address64 address) {

		Client client = clients.get(address);

		if (null == client) {
			Log.debug(LogLevel.VERBOSE,"ClientsManager", "search", "client " + client + " is NOT registered");
		} else {
			Log.debug(LogLevel.VERBOSE,"ClientsManager", "search", "client " + client + " is already registered");
		}

		return client;
	}

	synchronized Client save(final Client client) {

		if(null == client || null == client.address64) {
			Log.error("ClientManager", "save", "client = " + client + " & address64 = " + client.address64);
			return null;
		}

		if(null == clients.put(client.address64, client)){
			Log.debug(LogLevel.VERBOSE,"ClientManager", "save", "client " + client + " have been registered");
		} else {
			Log.debug(LogLevel.VERBOSE,"ClientManager", "save", "client " + client + " have been updated");
		}

		return client;
	}
}

