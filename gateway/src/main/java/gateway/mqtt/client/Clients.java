package gateway.mqtt.client;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
			client.start();
		} else {
			Log.debug(LogLevel.VERBOSE, "Clients", "search", "client " + client + " is already registered");
		}

		return client;
	}
}

