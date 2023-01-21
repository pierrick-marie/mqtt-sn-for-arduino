/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

/**
 * @TODO not implemented yet
 */
public class WillMessage implements Runnable {

	// private Device device;
	// private byte[] msg;

	public WillMessage(final Device device, final byte[] msg) {

		Log.xbeeInput(device, "Will message");

		// this.device = device;
		// this.msg = msg;
	}

	@Override
	public void run() {
		/*
		 * String willmessage = new String(msg, StandardCharsets.UTF_8);
		 * device.mqttClient().setWillMessage(willmessage);
		 */

		Log.error("WillMessage", "exec", "NOT IMPLEMENTED YET");
	}
}
