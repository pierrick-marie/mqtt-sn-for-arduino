package mqtt.sn;

import gateway.serial.SerialPortWriter;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;
import mqtt.MqttClient;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */

/**
 * This class is used to handle the connect message.
 */
public class Connect {

	private final Client client;
	private final byte[] message;

	public Connect(final Client client, final byte[] message) {

		Log.input(client, "connect");

		this.client = client;
		this.message = message;

		connect();
	}

	/**
	 * Method called after receiving a connect message.
	 **/
	public void connect() {

		byte flags = message[0];
		short duration = (short) (message[2] * 16 + message[3]);
		boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		if (client.name().equals("")) {
			String name = getClientName();
			Log.debug(LogLevel.ACTIVE, "Connect", "getClientName", "setup the client's name with " + name);
			client.setName(name);
		}

		Log.debug(LogLevel.ACTIVE, "Connect", "connect", client + " status is " + client.state());

		if (client.state().equals(utils.State.ASLEEP)) {
			Log.debug(LogLevel.ACTIVE, "Connect", "connect", "device " + client + " comes back from sleep");

			client.setState(utils.State.ACTIVE);

		} else if (client.state().equals(utils.State.LOST) || client.state().equals(utils.State.FIRSTCONNECT)) {

			client.setState(utils.State.ACTIVE);

			if (will) {
				createWillHandlers();
			}

			if( connectToTheBroker(cleanSession, duration) ) {
				connack(Prtcl.ACCEPTED);
			} else {
				connack(Prtcl.REJECTED);
			}
		}
	}

	private Boolean connectToTheBroker(final Boolean cleanSession, final Short duration) {

		Log.debug(LogLevel.ACTIVE, "SearchGateway", "connectToTheBroker", "connecting to the mqtt broker");

		MqttClient mqtt = new MqttClient();
		mqtt.setClientId(client.name());
		mqtt.setCleanSession(cleanSession);
		mqtt.setKeepAlive(duration);

		try {
			mqtt.connect();
		} catch (Exception e) {
			Log.debug(LogLevel.ACTIVE, "SearchGateway", "connectToTheBroker", "mqtt client not connected");
			Log.activeDebug(e.getMessage());

			return false;
		}
		Log.debug(LogLevel.ACTIVE, "SearchGateway", "connectToTheBroker", "connected");

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

	/**
	 * TODO: DEBUG
	 * Find the usage of this method.
	 */
	private void createWillHandlers() {

		Log.debug(LogLevel.ACTIVE, "Connect", "createWillHandlers", "");

		try {
			WillTopicReq willTopicReq = new WillTopicReq(client);
			willTopicReq.start();
			willTopicReq.join();
			WillMessageReq willMessageReq = new WillMessageReq(client);
			willMessageReq.start();
			willMessageReq.join();
		} catch (InterruptedException e) {
			Log.error("Connect", "createWillHandlers", "Exception while creating the \"will handlers\"");
			Log.debug(LogLevel.ACTIVE, "Connect", "createWillHandlers", e.getMessage());
		}
	}
}
