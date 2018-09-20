package main;

import gateway.Main;
import mqttsn.WillMessageReq;
import mqttsn.WillTopicReq;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import utils.Client;
import utils.log.Log;
import utils.log.LogLevel;
import utils.mqttclient.MqttClient;
import utils.mqttclient.MqttConnection;

import java.net.URISyntaxException;

public class TestMain {

	public static final long TIME_TO_WAIT = 10000; // 10 seconds
	public static final String HOST = "localhost";
	public static final Integer PORT = 1883;

	public static void main(String[] args) {

		// testMqttClientCallBack();

		testBlockingConnection();

		/*
		byte[] addrr = {0};
		Address64 a64 = new Address64(addrr);
		Address16 a16 = new Address16(addrr);

		Client client = new Client(a64, a16);

		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "Test create client: " + client);

		client.setState(utils.State.ACTIVE);

		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "new mqtt client");

		MQTT mqtt = createMqttClient(client, true, (short) 50);
		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "mqtt callback connection");

		Log.print(mqtt.getClientId().toString());
*/



		/*
		client.setMqttClient(mqtt);

		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "new callback connection");

		CallbackConnection connection = mqtt.callbackConnection();

		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "new mqtt listener");

		MqttListener listener = new MqttListener(client);
		connection.listener(listener);

		Log.debug(LogLevel.ACTIVE, "TestMain", "main", "new callback");

		connection.connect(new Callback<Void>() {
						 @Override
						 public void onSuccess(Void aVoid) {
							 Log.debug(LogLevel.ACTIVE, "TestMain", "Callback", "Test client: " + client);
						 }

						 @Override
						 public void onFailure(Throwable throwable) {
							 Log.debug(LogLevel.ACTIVE, "TestMain", "Callback", "Test client: " + client);
						 }
					 });
			// validCallBack.connack();

			client.setConnection(connection);
			*/
		// createWillHandlers(client);
	}

	private static void testBlockingConnection() {

		Log.acticeDebug("Connecting to Broker1 using MQTT");

		MqttClient mqtt = new MqttClient();
		try {
			mqtt.setHost(HOST, PORT);
			Log.acticeDebug("host is set");
		} catch (URISyntaxException e) {
			Log.acticeDebug(e.getMessage());
		}
		MqttConnection connection = mqtt.blockingConnection();
		Log.acticeDebug("new blocking connection");
		try {
			Log.acticeDebug("connect");
			connection.timeOutConnect(TIME_TO_WAIT);
			Log.acticeDebug("connected");
		} catch (Exception e) {
			Log.acticeDebug(e.getMessage());
		}
		Log.acticeDebug("Connected to Broker1");
		// Subscribe to  fidelityAds topic
		Topic[] topics = { new Topic("FIDELITY_ADS_TOPIC", QoS.AT_LEAST_ONCE)};
		try {
			connection.subscribe(topics);
			Log.acticeDebug("subscribe");
		} catch (Exception e) {
			Log.acticeDebug("Exception: " + e.getMessage());
		}
		// Publish Ads
		String ads1 = "Discount on transfert fees up to -50% with coupon code JBOSSDOCTOR.  www.beosbank.com";
		long index=0;
		while(true){
			try {
				connection.publish("FIDELITY_ADS_TOPIC", (index+":"+ads1).getBytes(), QoS.AT_LEAST_ONCE, false);
				Log.acticeDebug("publish");
			} catch (Exception e) {
				Log.acticeDebug(e.getMessage());

			}
			Log.acticeDebug("Sent messages with index = " + index);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.acticeDebug(e.getMessage());
			}
			index++;
			Log.acticeDebug("end of loop");
		}

	}


	private static void testMqttClientCallBack() {

		Log.acticeDebug("start testMqttClient method");

		MQTT mqtt = new MQTT();
		try {
			Log.acticeDebug("set host");
			mqtt.setHost(HOST, PORT);
		} catch (URISyntaxException e) {
			Log.acticeDebug(e.getMessage());
		}

		Log.acticeDebug("new connection");
		final CallbackConnection connection = mqtt.callbackConnection();

		Log.acticeDebug("setup connection");
		connection.listener(new Listener() {

			public void onDisconnected() {
				Log.acticeDebug("Listener - on disconnected");
			}

			public void onConnected() {
				Log.acticeDebug("Listener - on connected");
			}

			public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
				// You can now process a received message from a topic.
				// Once process execute the ack runnable.
				// ack.run();
				Log.acticeDebug("Listener - on publish");
			}

			public void onFailure(Throwable value) {
				// connection.close(null); // a connection failure occured.
				Log.acticeDebug("Listener - on failure");
			}
		});

		connection.connect(new Callback<Void>() {
			public void onFailure(Throwable value) {
				// result.failure(value); // If we could not connect to the server.
				Log.acticeDebug("Connect - on failure");
			}

			// Once we connect..
			public void onSuccess(Void v) {

				Log.acticeDebug("Connect - on success");

				// Subscribe to a topic
				Topic[] topics = {new Topic("foo", QoS.AT_LEAST_ONCE)};
				connection.subscribe(topics, new Callback<byte[]>() {
					public void onSuccess(byte[] qoses) {
						// The result of the subcribe request.
						Log.acticeDebug("Topic - on success");
					}

					public void onFailure(Throwable value) {
						//connection.close(null); // subscribe failed.
						Log.acticeDebug("Topic - on failure");
					}
				});

				// To disconnect..
				connection.disconnect(new Callback<Void>() {
					public void onSuccess(Void v) {
						// called once the connection is disconnected.
						Log.acticeDebug("disconnect - on success");
					}

					public void onFailure(Throwable value) {
						// Disconnects never fail.
						Log.acticeDebug("disconnect - on failure");
					}
				});
			}
		});

		Log.acticeDebug("publish a message");
		// Send a message to a topic
		connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
			public void onSuccess(Void v) {
				// the pubish operation completed successfully.
				Log.acticeDebug("Publish - on success");
			}

			public void onFailure(Throwable value) {
				//connection.close(null); // publish failed.
				Log.acticeDebug("Publish - on failure");
			}
		});

		Log.acticeDebug("end of method");
	}

	private static MQTT createMqttClient(final Client client, final Boolean cleanSession, final Short duration) {

		Log.debug(LogLevel.ACTIVE, "Connect", "createMqttClient", "Client " + client.name() + ": session = " + cleanSession + " duration = " + duration);

		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(Main.HOST, Main.PORT);
			mqtt.setClientId(client.name());
			mqtt.setCleanSession(cleanSession);
			mqtt.setKeepAlive(duration);
		} catch (URISyntaxException e) {
			Log.error("Connect", "createMqttClient", "Impossible to create the MQTT client");
			Log.debug(LogLevel.ACTIVE, "Connect", "createMqttClient", e.getMessage());
			Log.debug(LogLevel.ACTIVE, "Connect", "createMqttClient", e.getReason());
			return null;
		}

		return mqtt;
	}

	private static void createWillHandlers(final Client client) {

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