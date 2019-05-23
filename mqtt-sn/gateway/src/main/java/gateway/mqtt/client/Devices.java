/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.utils.log.Log;

public enum Devices {

	list;

	private static final List<Device> DEVICES = Collections.synchronizedList(new ArrayList<>());

	public synchronized Boolean remove(final Device device) {

		Log.error("Devices", "remvove", "Removing a device : " + device);

		return DEVICES.remove(device);
	}

	public synchronized Device search(final Address64 address64, final Address16 address16) {

		Log.debug("Devices", "search", "addr 64b: " + address64 + " - addr 16b: " + address16);

		Device device = null;

		for (final Device c : DEVICES) {
			if (c.address64().equals(address64)) {
				device = c;
				break;
			}
		}

		if (null == device) {
			Log.debug("Devices", "search", "device with address " + address64 + " is NOT registered");
			Log.debug("Devices", "search", "creating a new device");

			device = new Device(address64, address16, "Unnamed device " + DEVICES.size());
			DEVICES.add(device);
		} else {
			Log.debug("Devices", "search", device + " is already registered");
		}

		return device;
	}
}
