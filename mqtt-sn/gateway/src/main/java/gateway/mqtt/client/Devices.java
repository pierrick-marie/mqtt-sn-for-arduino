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
import gateway.utils.log.LogLevel;

public enum Devices {

	list;

	private static final List<Device> DEVICES = Collections.synchronizedList(new ArrayList<>());

	public synchronized Boolean remove(final Device device) {

		Log.error("Devices", "remvove", "Removing a device : " + device);

		return DEVICES.remove(device);
	}

	public synchronized Device search(final Address64 address64, final Address16 address16) {

		Device device = null;

		for (final Device c : DEVICES) {
			if (c.address64().equals(address64)) {
				device = c;
				break;
			}
		}

		if (null == device) {
			Log.debug(LogLevel.VERBOSE, "Devices", "search",
					"device with address " + address64 + " is NOT registered");
			Log.debug(LogLevel.ACTIVE, "Devices", "search", "creating a new device");

			device = new Device(address64, address16);
			DEVICES.add(device);
			// Doing this in RawDataParser switch case MessageType.SEARCHGW
			// device.start();
		} else {
			Log.debug(LogLevel.VERBOSE, "Devices", "search", "device " + device + " is already registered");
		}

		return device;
	}
}
