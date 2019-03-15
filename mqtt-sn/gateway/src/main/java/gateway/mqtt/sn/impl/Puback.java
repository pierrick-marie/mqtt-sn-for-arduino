/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.sn.IAction;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * @TODO not implemented yet
 */
public class Puback implements IAction {

	final Device device;
	final byte[] msg;

	public Puback(final Device device, final byte[] msg) {

		Log.input(device, "pub ack");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void exec() {

		if (msg[4] == (byte) 0x00) {
			final int msgID = (msg[3] << 8) + (msg[2] & 0xFF);

			Log.debug(LogLevel.ACTIVE, "PubAck", "puback", "message id ack: " + msgID);

			// NOT IMPLEMENTED YET (QoS)
			// device.acquitMessage(msgID);
		}
	}

}
