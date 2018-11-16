package gateway.mqtt.sn;

import gateway.serial.SerialPortWriter;
import gateway.mqtt.MqttClient;
import gateway.mqtt.client.DeviceState;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */

/**
 * This class is used to handle the connect message.
 */
public class Connect implements SnAction {

	private final Client client;
	private final byte[] message;

	public Connect(final Client client, final byte[] message) {

		Log.input(client, "connect");

		this.client = client;
		this.message = message;
	}

	@Override
	public void exec() {

		byte flags = message[0];
		short duration = (short) (message[2] * 16 + message[3]);
		// @Todo not implemented yet
		// boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		if (client.name().equals("")) {
			String name = getClientName();
			Log.debug(LogLevel.ACTIVE, "Connect", "getClientName", "setup the client's name with " + name);
			client.setClientName(name);
		}

		Log.debug(LogLevel.ACTIVE, "Connect", "connect", client + " status is " + client.state());

		if (client.state().equals(DeviceState.LOST) || client.state().equals(DeviceState.FIRSTCONNECT) || client.state().equals(DeviceState.DISCONNECTED)) {
			if (connectToTheBroker(cleanSession, duration)) {
				connack(Prtcl.ACCEPTED);
				client.setState(DeviceState.ACTIVE);
			} else {
				connack(Prtcl.REJECTED);
				client.setState(DeviceState.DISCONNECTED);
			}
		} else {
			// client's state is ACTIVE or AWAKE
			connack(Prtcl.ACCEPTED);
			client.setState(DeviceState.ACTIVE);
		}
	}

	private Boolean connectToTheBroker(final Boolean cleanSession, final Short duration) {

		Log.debug(LogLevel.ACTIVE, "Connect", "connectToTheBroker", "connecting to the gateway.mqtt broker");

		MqttClient mqtt = new MqttClient();
		mqtt.setClientId(client.name());
		mqtt.setCleanSession(cleanSession);
		mqtt.setKeepAlive(duration);

		try {
			mqtt.connect();
		} catch (Exception e) {
			Log.error("Connect", "connectToTheBroker", "gateway.mqtt client not connected");
			Log.activeDebug(e.getMessage());

			return false;
		}
		Log.debug(LogLevel.ACTIVE, "Connect", "connectToTheBroker", "connected");

		client.setMqttClient(mqtt);
		return true;
	}

	private void connack(final byte isConnected) {

		Log.output(client, "connack: " + isConnected);

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;
		serialMesasge[2] = isConnected;

		SerialPortWriter.write(client, serialMesasge);
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
