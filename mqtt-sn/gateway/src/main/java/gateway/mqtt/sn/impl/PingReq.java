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
public class PingReq implements IAction {

	final Device device;
	final byte[] msg;

	public PingReq(final Device device, final byte[] msg) {

		Log.input(device, "ping request");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void exec() {
		device.setState(DeviceState.AWAKE);

		Log.debug(LogLevel.ACTIVE, "PingReq", "exec", "begin send messages");

		// If send messages have not been interrupted, send pingresp
		if (device.sendMqttMessages()) {
			Log.debug(LogLevel.ACTIVE, "PingReq", "exec", "end send messages");

			pingresp();
		}
	}

	private void pingresp() {

		Log.output(device, "ping response");

		final byte[] ret = new byte[2];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x17;

		SerialPortWriter.write(device, ret);

		if (device.state().equals(DeviceState.AWAKE)) {
			device.setState(DeviceState.ASLEEP);
			Log.debug(LogLevel.ACTIVE, "MultipleSender", "pingResp", device + " goes to sleep");
		}
	}
}
