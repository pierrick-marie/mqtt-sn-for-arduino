package utils.client;

import utils.address.Address;
import utils.address.Address16;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;

import java.util.*;

public enum Clients {

	list;

	private static final List<Client> clients = Collections.synchronizedList(new ArrayList<>());

	public synchronized Client search(final Address64 address64, final Address16 address16) {

		Client client = null;

		for(Client c : clients){
			if(c.address64.equals(address64)) {
				client = c;
				break;
			}
		}

		if (null == client) {
			Log.debug(LogLevel.VERBOSE, "Clients", "search", "client with address " + address64 + " is NOT registered");
			Log.debug(LogLevel.ACTIVE, "Clients", "search", "creating a new client");

			client = new Client(address64, address16);
			clients.add(client);
		} else {
			Log.debug(LogLevel.VERBOSE, "Clients", "search", "client " + client + " is already registered");
		}

		return client;
	}
}

