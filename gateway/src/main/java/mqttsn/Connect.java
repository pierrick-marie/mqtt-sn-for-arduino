package mqttsn;

import gateway.Main;
import gateway.MqttListener;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.Log;
import utils.State;
import utils.Time;

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

		MqttCallback validCallBack = new MqttCallback(address64, address16, true);
		MqttCallback invalidCallBack = new MqttCallback(address64, address16, false);

		String clientId = getClientId();

		MQTT mqtt = createMqttClient(clientId, cleanSession, duration);
		if(null == mqtt){
			Log.error("Connect", "connect", "mqtt client is null");
			return;
		}

		if (Main.ClientMap.containsKey(clientId)) {
			Log.debug("Connect", "connect","The client is already knew and its status is \"sleeping\"");

			if (Main.ClientState.get(Utils.byteArrayToString(address64)).equals(utils.State.ASLEEP)) {
				Log.debug("Connect", "connect","device = " + Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " come back from sleep");

				// @DEBUG SAME CODE
				Main.ClientState.put(Utils.byteArrayToString(address64), utils.State.ACTIVE);
				// @DEBUG SAME CODE

				Time.sleep((long) 10, "Connect.connect(): An error occurs when trying to sleep the current thread");

				validCallBack.connack();

			} else if (Main.ClientState.get(Utils.byteArrayToString(address64)).equals(utils.State.LOST)) {
				Log.debug("Connect", "connect","The client is knew and its status is \"lost\"");


				// @DEBUG SAME CODE
				Main.ClientState.put(Utils.byteArrayToString(address64), utils.State.ACTIVE);
				CallbackConnection connection = mqtt.callbackConnection();
				Main.AddressConnectionMap.put(Utils.byteArrayToString(address64), connection);
				MqttListener listener = new MqttListener(address64);
				connection.listener(listener);
				// @DEBUG SAME CODE

				connection.connect(invalidCallBack);
				if (will) {
					createWillHandlers();
				}
			} else {
				validCallBack.connack();
			}
		} else {
			Log.debug("Connect", "connect","The client is not knew");

			Main.AddressClientMap.put(Utils.byteArrayToString(address64), clientId);
			Main.ClientMap.put(clientId, mqtt);

			// @DEBUG SAME CODE
			Main.ClientState.put(Utils.byteArrayToString(address64), utils.State.ACTIVE);
			CallbackConnection connection = mqtt.callbackConnection();
			Main.AddressConnectionMap.put(Utils.byteArrayToString(address64), connection);
			MqttListener listener = new MqttListener(address64);
			connection.listener(listener);
			// @DEBUG SAME CODE

			// @DEBUG Valid the first connexion of the client
			connection.connect(validCallBack);
			validCallBack.connack();

			if (will) {
				createWillHandlers();
			}
		}
	}

	private String getClientId() {

		Log.debug("Connect", "getClientId","searching the client id");

		byte[] id = new byte[message.length - 4];
		for (int i = 0; i < id.length; i++) {
			id[i] = message[4 + i];
		}

		String clientId = new String(id, StandardCharsets.UTF_8);
		Log.debug("Connect", "createMqttClient","clientId = " + clientId);
		return clientId;
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
			Log.error("Connect", "createWillHandlers", "exception while creating the \"will handlers\"");
			Log.debug("Connect", "createWillHandlers", e.getMessage());
		}
	}

	private MQTT createMqttClient(final String clientId, final Boolean cleanSession, final Short duration) {

		Log.debug("Connect", "createMqttClient", "clientId = " + clientId + " cleanSession = " + cleanSession + " duration = " + duration);

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(Main.HOST, Main.PORT);
			mqtt.setClientId(clientId);
			mqtt.setCleanSession(cleanSession);
			mqtt.setKeepAlive(duration);
		} catch (URISyntaxException e) {
			Log.error("Connect", "createMqttClient", "impossible to create the MQTT client");
			Log.debug("Connect", "createMqttClient", e.getMessage());
			Log.debug("Connect", "createMqttClient", e.getReason());
			return null;
		}

		return mqtt;
	}
}
