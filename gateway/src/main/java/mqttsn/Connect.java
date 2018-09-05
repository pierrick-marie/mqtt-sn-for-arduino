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
 *
 * @todo DEBUG
 */
public class Connect extends Thread {

	private final byte[] address64;
	private final byte[] address16;
	private final byte[] message;

	public Connect(byte[] address64, byte[] address16, byte[] message) {
		this.address16 = address16;
		this.address64 = address64;
		this.message = message;
	}

	public void run() {
		connect();
	}

	/**
	 * Method called after receiving a connect message.
	 *
	 * @todo DEBUG: use the ClientsManager instead
	 **/
	public void connect()  {

		byte flags = message[0];
		short duration = (short) (message[2] * 16 + message[3]);
		boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		String clientName = getClientName();

		Client client = ClientsManager.Instance.getClient(clientName);

		if (null != client) {
			Log.debug("Connect", "connect",client + " is known and its status is " + client.state());

			MqttCallback validCallBack = new MqttCallback(client, true);

			if (client.state().equals(utils.State.ASLEEP)) {
				Log.debug("Connect", "connect","device " + client + " comes back from sleep");

				client.setState(utils.State.ACTIVE);

				Time.sleep((long) 10, "Connect.connect(): An error occurs when trying to sleep the current thread");

				validCallBack.connack();

			} else if (client.state().equals(utils.State.LOST)) {

				client.setState(utils.State.ACTIVE);

				MQTT mqtt = client.mqttClient();
				if(null == mqtt){
					Log.error("Connect", "connect", "mqtt client is null");
					mqtt = createMqttClient(clientName, cleanSession, duration);
					client.setMqttClient(mqtt);
				}

				CallbackConnection connection = mqtt.callbackConnection();
				MqttListener listener = new MqttListener(address64);
				connection.listener(listener);

				MqttCallback invalidCallBack = new MqttCallback(client, false);
				connection.connect(invalidCallBack);

				client.setConnection(connection);

				if (will) { createWillHandlers(); }

			} else {
				validCallBack.connack();
			}
		} else {
			Log.debug("Connect", "connect","Client " + clientName + " is unknown -> creating a new client");

			client = ClientsManager.Instance.newClient(clientName);
			client.setAddress64(address64);
			client.setAddress16(address16);
			client.setState(utils.State.ACTIVE);

			MQTT mqtt = createMqttClient(clientName, cleanSession, duration);
			client.setMqttClient(mqtt);

			CallbackConnection connection = mqtt.callbackConnection();
			MqttListener listener = new MqttListener(address64);
			connection.listener(listener);
			client.setConnection(connection);

			MqttCallback validCallBack = new MqttCallback(client, true);
			connection.connect(validCallBack);
			validCallBack.connack();

			if (will) { createWillHandlers(); }
		}
		Log.debug("Connect", "connect","End of method with client " + client);
	}

	private String getClientName() {

		Log.debug("Connect", "getClientName","Searching the client's name");

		byte[] name = new byte[message.length - 4];

		for (int i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		String clientName = new String(name, StandardCharsets.UTF_8);
		Log.debug("Connect", "getClientName","Client's name is " + clientName);
		return clientName;
	}

	private void createWillHandlers() {

		Log.debug("Connect", "createWillHandlers","");

		try {
			WillTopicReq willTopicReq = new WillTopicReq(address64, address16);
			willTopicReq.start();
			willTopicReq.join();
			WillMessageReq willMessageReq = new WillMessageReq(address64, address16);
			willMessageReq.start();
			willMessageReq.join();
		} catch (InterruptedException e) {
			Log.error("Connect", "createWillHandlers", "Exception while creating the \"will handlers\"");
			Log.debug("Connect", "createWillHandlers", e.getMessage());
		}
	}

	private MQTT createMqttClient(final String clientName, final Boolean cleanSession, final Short duration) {

		Log.debug("Connect", "createMqttClient", "Client " + clientName + ": session = " + cleanSession + " duration = " + duration);

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(Main.HOST, Main.PORT);
			mqtt.setClientId(clientName);
			mqtt.setCleanSession(cleanSession);
			mqtt.setKeepAlive(duration);
		} catch (URISyntaxException e) {
			Log.error("Connect", "createMqttClient", "Impossible to create the MQTT client");
			Log.debug("Connect", "createMqttClient", e.getMessage());
			Log.debug("Connect", "createMqttClient", e.getReason());
			return null;
		}

		return mqtt;
	}
}
