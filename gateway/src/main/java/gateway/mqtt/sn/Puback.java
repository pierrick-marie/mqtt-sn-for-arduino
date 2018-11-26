package gateway.mqtt.sn;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not used until with QoS level 1 and 2 (not implemented)
 */
public class Puback implements SnAction {

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
			int msgID = (msg[3] << 8) + (msg[2] & 0xFF);

			Log.debug(LogLevel.ACTIVE, "PubAck", "puback","message id ack: " + msgID);

			device.acquitMessage(msgID);
		}
	}



}
