package utils.client;

import utils.address.Address;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;

import java.util.HashMap;

enum Clients {

	list;

	private final HashMap<Address, Client> clients = new HashMap<>();

	synchronized Client search(final Address64 address) {

		Client client = clients.get(address);

		if (null == client) {
			Log.debug(LogLevel.VERBOSE,"Clients", "search", "client with address " + address + " is NOT registered");
		} else {
			Log.debug(LogLevel.VERBOSE,"Clients", "search", "client " + client + " is already registered");
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

