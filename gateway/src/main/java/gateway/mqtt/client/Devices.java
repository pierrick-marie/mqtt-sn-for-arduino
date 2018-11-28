/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.client;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Devices {

	list;

	private static final List<Device> DEVICES = Collections.synchronizedList(new ArrayList<>());

	public synchronized Device search(final Address64 address64, final Address16 address16) {

		Device device = null;

		for(Device c : DEVICES){
			if(c.address64.equals(address64)) {
				device = c;
				break;
			}
		}

		if (null == device) {
			Log.debug(LogLevel.VERBOSE, "Devices", "search", "device with address " + address64 + " is NOT registered");
			Log.debug(LogLevel.ACTIVE, "Devices", "search", "creating a new device");

			device = new Device(address64, address16);
			DEVICES.add(device);
			device.start();
		} else {
			Log.debug(LogLevel.VERBOSE, "Devices", "search", "device " + device + " is already registered");
		}

		return device;
	}
}

