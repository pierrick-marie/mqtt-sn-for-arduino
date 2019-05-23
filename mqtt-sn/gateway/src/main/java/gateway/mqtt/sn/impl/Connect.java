/**
 * Created by arnaudoglaza on 07/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import java.nio.charset.StandardCharsets;

import gateway.mqtt.client.Device;
import gateway.mqtt.client.DeviceState;
import gateway.serial.SerialPortWriter;
import gateway.utils.log.Log;

public class Connect implements Runnable {

	private final Device device;
	private final byte[] message;

	public Connect(final Device device, final byte[] message) {

		Log.input(device, "connect");

		this.device = device;
		this.message = message;
	}

	private void connack(final byte isConnected) {

		Log.output(device, "connack " + isConnected);

		final byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;
		serialMesasge[2] = isConnected;

		SerialPortWriter.write(device, serialMesasge);
	}

	private String getClientName() {

		final byte[] name = new byte[message.length - 4];

		for (int i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		final String clientName = new String(name, StandardCharsets.UTF_8);

		return clientName;
	}

	@Override
	public void run() {

		final byte flags = message[0];
		final short duration = (short) (message[2] * 16 + message[3]);
		// boolean will = (flags >> 3) == 1;
		final boolean cleanSession = flags >> 2 == 1;

		final String name = getClientName();
		Log.info(device + "'s name is now " + name);
		device.setName(name);

		Log.info(name + " is " + device.state());

		if (device.state().equals(DeviceState.LOST) || device.state().equals(DeviceState.FIRSTCONNECT)
				|| device.state().equals(DeviceState.DISCONNECTED)) {

			device.initMqttClient(cleanSession);

			if (device.connect(duration)) {
				connack(Prtcl.ACCEPTED);
				Log.info(name + " is " + DeviceState.ACTIVE);
				device.setState(DeviceState.ACTIVE);
			} else {
				Log.error("Connect", "connect", device + " not connected to the broker");
				connack(Prtcl.REJECTED);
				Log.info(name + " is " + DeviceState.DISCONNECTED);
				device.setState(DeviceState.DISCONNECTED);
			}

		} else {
			// device's state is ACTIVE or AWAKE
			connack(Prtcl.ACCEPTED);
			Log.info(name + " is " + DeviceState.ACTIVE);
			device.setState(DeviceState.ACTIVE);
		}
	}
}
