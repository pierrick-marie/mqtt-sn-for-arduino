/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.client.DeviceState;
import gateway.mqtt.impl.Sender;
import gateway.mqtt.impl.SnMessage;
import gateway.serial.Writer;
import gateway.utils.log.Log;

public class PingReq implements Runnable {

	private final long WAIT_SENDING_NEXT_MESSAGE = 1000; // 1000 milliseconds (1 seconds)

	final Device device;
	final byte[] msg;

	public PingReq(final Device device, final byte[] msg) {

		Log.xbeeInput(device, "ping request");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void run() {
		device.setState(DeviceState.AWAKE);

		Log.debug("PingReq", "run", "begin send messages");

		// If send messages have not been interrupted, send pingresp
		if (sendMqttMessages()) {
			Log.debug("PingReq", "run", "end send messages");

			Log.xbeeOutput(device, "ping response");

			final byte[] ret = new byte[2];
			ret[0] = (byte) 0x03;
			ret[1] = (byte) 0x17;

			Writer.Instance.write(device, ret);

			if (device.state().equals(DeviceState.AWAKE)) {
				device.setState(DeviceState.ASLEEP);
			}
		}
	}

	/**
	 * The function is call after a PingReq. If it returns "false", pingreq have to
	 * stop the rest of its instructions -> the device have been reset.
	 *
	 * The function can be interrupted at any time by a reboot of the arduino.
	 *
	 * @return false if the device have been reset, true otherwise.
	 */
	private Boolean sendMqttMessages() {

		final Sender sender = new Sender(device);

		synchronized (device.Messages) {
			for (final SnMessage message : device.Messages) {

				Log.xbeeOutput(device, new String(message.getPayload()) + " on topic " + message.topic());

				Log.info(device + " get message " + new String(message.getPayload()) + " on topic "
						+ message.topic());
				sender.send(message);

				Log.debug("PingReq", "sendMqttMessages", "wait before sending next message");

				try {
					Thread.sleep(WAIT_SENDING_NEXT_MESSAGE);
				} catch (final InterruptedException e) {
					Log.error("PingReq", "sendMqttMessages", "fail waiting before sending next message");
					Log.debug("PingReq", "sendMqttMessages", e.getMessage());
					return false;
				}
			}
			Log.debug("PingReq", "sendMqttMessages", "all messages have been sent");
			device.Messages.clear();
			return true;
		}
	}
}
