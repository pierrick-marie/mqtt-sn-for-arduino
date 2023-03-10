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
public class Puback implements Runnable {

	final Device device;
	final byte[] msg;

	public Puback(final Device device, final byte[] msg) {

		Log.xbeeInput(device, "pub ack");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void run() {

		if (msg[4] == (byte) 0x00) {
			final int msgID = (msg[3] << 8) + (msg[2] & 0xFF);

			Log.debug("PubAck", "puback", "message id ack " + msgID);

			// NOT IMPLEMENTED YET (QoS)
			// device.acquitMessage(msgID);
		}
	}

}
