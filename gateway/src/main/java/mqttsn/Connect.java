package mqttsn;

import gateway.Main;
import gateway.Mqtt_Listener;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.Log;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */

/**
 * This class is used to handle the connect message.
 *
 * @todo DEBUG
 */
public class Connect extends Thread {

	byte[] address64;
	byte[] address16;
	byte[] message;

	public Connect(byte[] address64, byte[] address16, byte[] message) {
		this.address16 = address16;
		this.address64 = address64;
		this.message = message;
	}

	private MQTT createMqttClient(final String clientId, final Boolean cleanSession, final Short duration) {

		Log.debug("Connect.java -> creating a MQTT client with: " + clientId + ", " + cleanSession + ", " + duration);

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(Main.HOST, Main.PORT);
			mqtt.setClientId(clientId);
			mqtt.setCleanSession(cleanSession);
			mqtt.setKeepAlive(duration);
		} catch (URISyntaxException e) {
			Log.debug(e.getMessage());
			Log.debug(e.getReason());
		}

		return mqtt;
	}

	/**
	 * Method called after receiving a connect message.
	 *
	 * @todo DEBUG
	 *
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void connect() throws URISyntaxException, InterruptedException {

		// @todo DEBUG: null Connect for the first connection of the device
		// Log.print(Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " BLA BAL BAL Connect");


		byte flags = message[0];
		short duration = (short) (message[2] * 16 + message[3]);
		boolean will = (flags >> 3) == 1;
		boolean cleanSession = (flags >> 2) == 1;

		Log.debug("Connect.java -> searching the client id");
		byte[] id = new byte[message.length - 4];
		for (int i = 0; i < id.length; i++) {
			id[i] = message[4 + i];
		}
		String clientId = new String(id, StandardCharsets.UTF_8);
		Log.debug("Connect.java -> connect with client id: " + clientId);

		MQTT mqtt = createMqttClient(clientId, cleanSession, duration);

		// The client is already knowed
		if (Main.ClientMap.containsKey(clientId)) {
			if (Main.ClientState.get(Utils.byteArrayToString(address64)).equals("Asleep")) {
				Log.debug("Connect.java -> device " + Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " come back from sleep");
				Main.ClientState.put(Utils.byteArrayToString(address64), "Active");
				Thread.sleep(10);
				connack(true);
			} else if (Main.ClientState.get(Utils.byteArrayToString(address64)).equals("Lost")) {


				CallbackConnection connection = mqtt.callbackConnection();
				Main.AddressConnectiontMap.put(Utils.byteArrayToString(address64), connection);
				Main.ClientState.put(Utils.byteArrayToString(address64), "Active");
				Mqtt_Listener listener = new Mqtt_Listener(address64);
				connection.listener(listener);
				if (will) {
					//System.out.println("before topicreq");
					WillTopicReq willTopicReq = new WillTopicReq(address64, address16);
					willTopicReq.start();
					willTopicReq.join();
					//System.out.println("after topicreq");
					WillMessageReq willMessageReq = new WillMessageReq(address64, address16);
					willMessageReq.start();
					willMessageReq.join();
					//System.out.println("after msgreq");
				}
				connection.connect(new Callback<Void>() {
					@Override
					public void onSuccess(Void value) {
						connack(true);
					}

					@Override
					public void onFailure(Throwable value) {
						Log.print("Failure on connect");
						value.printStackTrace();
					}
				});
			} else {
				connack(true);
			}
		} else {
			// The client is not knowed

			CallbackConnection connection = mqtt.callbackConnection();
			Main.AddressClientMap.put(Utils.byteArrayToString(address64), clientId);
			Main.ClientMap.put(clientId, mqtt);
			Main.AddressConnectiontMap.put(Utils.byteArrayToString(address64), connection);
			Main.ClientState.put(Utils.byteArrayToString(address64), "Active");
			Mqtt_Listener listener = new Mqtt_Listener(address64);
			connection.listener(listener);
			if (will) {
				//System.out.println("before topicreq");
				WillTopicReq willTopicReq = new WillTopicReq(address64, address16);
				willTopicReq.start();
				willTopicReq.join();
				//System.out.println("after topicreq");
				WillMessageReq willMessageReq = new WillMessageReq(address64, address16);
				willMessageReq.start();
				willMessageReq.join();
				//System.out.println("after msgreq");
			}
			connection.connect(new Callback<Void>() {
				@Override
				public void onSuccess(Void value) {
					connack(false);
				}

				@Override
				public void onFailure(Throwable value) {
					Log.print("Failure on connect");
					value.printStackTrace();
				}
			});
		}
	}

	public void connack(final Boolean isValid) {

		Log.print("<- " + Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " Connack");

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;

		if (isValid) {
			serialMesasge[2] = (byte) 0x00;
		} else {
			serialMesasge[2] = (byte) 0x03;
		}

		Serial.write(Main.SerialPort, address64, address16, serialMesasge);

	}

	public void run() {
		try {
			connect();
		} catch (URISyntaxException e) {
			Log.debug(e.getMessage());
			Log.debug(e.getReason());
		} catch (InterruptedException e) {
			Log.debug(e.getMessage());
		}
	}
}
