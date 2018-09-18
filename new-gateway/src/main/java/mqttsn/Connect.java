package mqttsn;

import gateway.Main;
import gateway.MqttListener;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.*;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */

/**
 * This class is used to handle the connect message.
 */
public class Connect extends Thread {

	private final Client client;
	private final byte[] message;

	public Connect(final Client client, final byte[] message) {
		this.client = client;
		this.message = message;
	}

	public void run() {
		connect();
	}

	/**
	 * Method called after receiving a connect message.
	 **/
	public void connect()  {

		byte flags = message[0];
		short duration = (short) (message[2] * 16 + message[3]);
		boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		if(client.name().equals("")) {
			String name = getClientName();
			Log.debug(LogLevel.ACTIVATED,"Connect", "getClientName","setup the client's name with " + name);
			client.setName(name);
		}

		Log.input(client, "connect");

		if (null != client) {
			Log.debug(LogLevel.ACTIVATED,"Connect", "connect",client + " is known and its status is " + client.state());

			MqttCallback validCallBack = new MqttCallback(client, true);

			if (client.state().equals(utils.State.ASLEEP)) {
				Log.debug(LogLevel.ACTIVATED,"Connect", "connect","device " + client + " comes back from sleep");

				client.setState(utils.State.ACTIVE);

				Time.sleep((long) 10, "Connect.connect(): An error occurs when trying to sleep the current thread");

				validCallBack.connack();

			} else if (client.state().equals(utils.State.LOST)) {

				client.setState(utils.State.ACTIVE);

				MQTT mqtt = client.mqttClient();
				if(null == mqtt){
					Log.error("Connect", "connect", "mqtt client is null");
					mqtt = createMqttClient(cleanSession, duration);
					client.setMqttClient(mqtt);
				}

				CallbackConnection connection = mqtt.callbackConnection();
				MqttListener listener = new MqttListener(client);
				connection.listener(listener);

				MqttCallback invalidCallBack = new MqttCallback(client, false);
				connection.connect(invalidCallBack);

				client.setConnection(connection);

				if (will) { createWillHandlers(); }

			} else {
				validCallBack.connack();
			}
		} else {
			Log.debug(LogLevel.ACTIVATED,"Connect", "connect","Client " + client + " is unknown -> creating a new client");

			client.setState(utils.State.ACTIVE);

			MQTT mqtt = createMqttClient(cleanSession, duration);
			client.setMqttClient(mqtt);

			CallbackConnection connection = mqtt.callbackConnection();
			MqttListener listener = new MqttListener(client);
			connection.listener(listener);
			client.setConnection(connection);

			MqttCallback validCallBack = new MqttCallback(client, true);
			connection.connect(validCallBack);
			validCallBack.connack();

			if (will) { createWillHandlers(); }
		}
	}

	private String getClientName() {

		byte[] name = new byte[message.length - 4];

		for (int i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		String clientName = new String(name, StandardCharsets.UTF_8);

		return clientName;
	}

	private void createWillHandlers() {

		Log.debug(LogLevel.ACTIVATED,"Connect", "createWillHandlers","");

		try {
			WillTopicReq willTopicReq = new WillTopicReq(client);
			willTopicReq.start();
			willTopicReq.join();
			WillMessageReq willMessageReq = new WillMessageReq(client);
			willMessageReq.start();
			willMessageReq.join();
		} catch (InterruptedException e) {
			Log.error("Connect", "createWillHandlers", "Exception while creating the \"will handlers\"");
			Log.debug(LogLevel.ACTIVATED,"Connect", "createWillHandlers", e.getMessage());
		}
	}

	private MQTT createMqttClient(final Boolean cleanSession, final Short duration) {

		Log.debug(LogLevel.ACTIVATED,"Connect", "createMqttClient", "Client " + client.name() + ": session = " + cleanSession + " duration = " + duration);

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(Main.HOST, Main.PORT);
			mqtt.setClientId(client.name());
			mqtt.setCleanSession(cleanSession);
			mqtt.setKeepAlive(duration);
		} catch (URISyntaxException e) {
			Log.error("Connect", "createMqttClient", "Impossible to create the MQTT client");
			Log.debug(LogLevel.ACTIVATED,"Connect", "createMqttClient", e.getMessage());
			Log.debug(LogLevel.ACTIVATED,"Connect", "createMqttClient", e.getReason());
			return null;
		}

		return mqtt;
	}
}
