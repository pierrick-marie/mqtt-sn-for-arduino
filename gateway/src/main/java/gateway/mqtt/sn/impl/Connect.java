/**
 * Created by arnaudoglaza on 07/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.impl.Client;
import gateway.mqtt.client.Device;
import gateway.mqtt.sn.IAction;
import gateway.serial.SerialPortWriter;
import gateway.mqtt.client.DeviceState;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.nio.charset.StandardCharsets;

public class Connect implements IAction {

	private final Device device;
	private final byte[] message;

	public Connect(final Device device, final byte[] message) {

		Log.input(device, "connect");

		this.device = device;
		this.message = message;
	}

	@Override
	public void exec() {

		byte flags = message[0];
		// @Todo not implemented yet
       	// short duration = (short) (message[2] * 16 + message[3]);
        	// boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		String name = getClientName();
		Log.debug(LogLevel.ACTIVE, "Connect", "getClientName", "setup the device's name with " + name);
		device.setName(name);

		Log.debug(LogLevel.ACTIVE, "Connect", "connect", device + " status is " + device.state());

		if (device.state().equals(DeviceState.LOST) || device.state().equals(DeviceState.FIRSTCONNECT) || device.state().equals(DeviceState.DISCONNECTED)) {

            Client mqtt = new Client(device, cleanSession);

            if(mqtt.connect()) {
                Log.debug(LogLevel.ACTIVE, "Connect", "connectToTheBroker", "connected");
                device.setMqttClient(mqtt);
                device.setState(DeviceState.ACTIVE);
                connack(Prtcl.ACCEPTED);
            } else {
                Log.debug(LogLevel.ACTIVE, "Connect", "connectToTheBroker", "device not connected");
                device.setState(DeviceState.DISCONNECTED);
                connack(Prtcl.REJECTED);
            }

		} else {
			// device's state is ACTIVE or AWAKE
			connack(Prtcl.ACCEPTED);
			device.setState(DeviceState.ACTIVE);
		}
	}

	private void connack(final byte isConnected) {

		Log.output(device, "connack: " + isConnected);

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;
		serialMesasge[2] = isConnected;

		SerialPortWriter.write(device, serialMesasge);
	}

	private String getClientName() {

		byte[] name = new byte[message.length - 4];

		for (int i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		String clientName = new String(name, StandardCharsets.UTF_8);

		return clientName;
	}
}
