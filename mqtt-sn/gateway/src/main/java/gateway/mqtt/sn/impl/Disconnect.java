package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.client.DeviceState;
import gateway.mqtt.sn.IAction;
import gateway.serial.SerialPortWriter;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Disconnect implements IAction {

	private final Device device;
	private final byte[] msg;

	public Disconnect(final Device device, final byte[] msg) {

		Log.input(device, "disconnect");

		this.device = device;
		this.msg = msg;
	}

	private void disconnectAck() {

		Log.input(device, "Disconnect Ack");

		final byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x18;

		SerialPortWriter.write(device, ret);
	}

	@Override
	public void exec() {

		if (msg.length == 4) {
			final short duration = (short) ((msg[0] << 8) + (msg[1] & 0xFF));

			if (duration > 0) {

				device.setState(DeviceState.ASLEEP).setDuration(duration);

				disconnectAck();

				Log.debug(LogLevel.ACTIVE, "Disconnect", "diconnect",
						"Going into sleep with duration: " + duration);
			}
		} else {
			device.setState(DeviceState.DISCONNECTED);
			disconnectAck();
		}
	}
}
