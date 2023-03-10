/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.client.DeviceState;
import gateway.serial.Writer;
import gateway.utils.log.Log;

public class Disconnect implements Runnable {

	private final Device device;
	private final byte[] msg;

	public Disconnect(final Device device, final byte[] msg) {

		Log.xbeeInput(device, "disconnect");

		this.device = device;
		this.msg = msg;
	}

	private void disconnectAck() {

		Log.xbeeInput(device, "disconnect Ack");

		final byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x18;

		Writer.Instance.write(device, ret);
	}

	@Override
	public void run() {

		if (msg.length == 4) {
			final short duration = (short) ((msg[0] << 8) + (msg[1] & 0xFF));

			if (duration > 0) {

				device.setState(DeviceState.ASLEEP);

				Log.info(device + " disconnected \n");
				device.setDuration(duration);

				disconnectAck();

				Log.debug("Disconnect", "diconnect", "Going into sleep with duration: " + duration);
			}
		} else {
			device.setState(DeviceState.DISCONNECTED);
			disconnectAck();
		}
	}
}
