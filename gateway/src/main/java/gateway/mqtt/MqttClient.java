package gateway.mqtt;

import gateway.mqtt.sn.Prtcl;
import gateway.utils.Config;
import org.fusesource.mqtt.client.*;
import gateway.utils.Time;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static gateway.mqtt.sn.Prtcl.PAYLOAD_LENGTH;

public class MqttClient extends MQTT {

	private static final String HOST = Config.IP_SERVER;
	private static final Integer PORT = Config.PORT_SERVER;

	private final long TIME_TO_WAIT = 500; // 0.5 seconds
	private final short NB_TRY = 5;

	private final BlockingConnection connection;
	private Boolean isConnected = false;

	class ThreadConnect extends Thread {

		public Boolean isConnected;

		public ThreadConnect() {
			isConnected = false;
		}

		public void run() {
			try {
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", "starting a new connection to the gateway.mqtt broker");
				connection.connect();
				isConnected = true;
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", "connection to the gateway.mqtt broker activated");
			} catch (Exception e) {
				Log.error("Inner class: ThreadConnect", "run", "connection to the gateway.mqtt broker impossible");
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", e.getMessage());
			}
		}

		public synchronized void stopConnection() {
			Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "stopConnection", "stopping the current thread");
			Thread.currentThread().interrupt();
		}
	}

	class ThreadSubscribe extends Thread {

		public Boolean isSubscribed;
		private String topicName;

		public ThreadSubscribe(final String topicName) {
			isSubscribed = false;
			this.topicName = topicName;
		}

		public void run() {
			try {
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadSubscribe", "run", "start to subscribe the topic: " + topicName);
				connection.subscribe(new Topic[]{new Topic(topicName, Prtcl.DEFAUlT_QOS)});
				isSubscribed = true;
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadSubscribe", "run", "subscription ok");
			} catch (Exception e) {
				Log.error("Inner class: ThreadConnect", "run", "subscription NOT ok");
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", e.getMessage());
			}
		}

		public synchronized void stopSubscribe() {
			Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "stopSubscribe", "stopping the current thread");
			Thread.currentThread().interrupt();
		}
	}

	class ThreadListenMessage extends Thread {

		private final Client client;

		public ThreadListenMessage(final Client client) {
			this.client = client;
		}

		public void run() {
			Message message = null;
			try {
				while (true) {
					message = connection.receive();
					String payload = new String(message.getPayload());
					Log.print("MQTT message received: " + payload + " on topic: " + message.getTopic());

					if (payload.length() < PAYLOAD_LENGTH) {
						MqttMessage mqttMessage = new MqttMessage(message.getTopic(), payload);
						client.addMqttMessage(mqttMessage);
					} else {
						Log.error("MqttClient.ThreadListenMessage", "run", "payload too long");
					}
					message.ack();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public MqttClient() {
		try {
			setHost(HOST, PORT);
		} catch (URISyntaxException e) {
			Log.error("MqttClient", "constructor", "Impossible to set host: " + HOST + ":" + PORT);
			Log.debug(LogLevel.VERBOSE, "MqttClient", "constructor", e.getMessage());
		}
		connection = this.blockingConnection();
	}

	public void connect() throws TimeoutException {

		Log.debug(LogLevel.ACTIVE, "MqttClient", "connect", "try to connect to the gateway.mqtt broker");

		ThreadConnect threadConnection = new ThreadConnect();
		threadConnection.start();
		long time = System.currentTimeMillis();

		while (time + NB_TRY * TIME_TO_WAIT > System.currentTimeMillis() && false == threadConnection.isConnected) {
			Time.sleep(TIME_TO_WAIT, "time out connection");
		}

		if (!threadConnection.isConnected) {
			Log.error("MqttClient", "connect", "time out - stop connection to gateway.mqtt server");
			threadConnection.stopConnection();
			TimeoutException e = new TimeoutException("impossible to reach the gateway.mqtt server");
			throw e;
		}

		Log.debug(LogLevel.ACTIVE, "MqttClient", "connect", "connected to the gateway.mqtt broker");
		isConnected = true;
	}

	public void subscribe(final Client client, final SnTopic topic) throws TimeoutException {

		Log.debug(LogLevel.ACTIVE, "MqttClient", "subscribe", "try to subscribe to the topic: " + topic.name());

		ThreadSubscribe threadSubscribe = new ThreadSubscribe(topic.name().toString());
		threadSubscribe.start();
		long time = System.currentTimeMillis();

		while (time + NB_TRY * TIME_TO_WAIT > System.currentTimeMillis() && false == threadSubscribe.isSubscribed) {
			Time.sleep(TIME_TO_WAIT, "time out connection");
		}

		if (!threadSubscribe.isSubscribed) {
			Log.error("MqttClient", "subscribe", "time out - stop subscription to gateway.mqtt server");
			threadSubscribe.stopSubscribe();
			TimeoutException e = new TimeoutException("impossible to subscribe " + topic.name() + " to the gateway.mqtt server");
			throw e;
		}

		Log.debug(LogLevel.ACTIVE, "MqttClient", "subscribe", "subscription to the gateway.mqtt broker OK");

		ThreadListenMessage threadListenMessage = new ThreadListenMessage(client);
		threadListenMessage.start();
		topic.setSubscribed();
	}

	public Boolean isConnected() {
		return isConnected;
	}

	public Boolean publish(final String topic, final byte[] message, final Boolean retain) {
		try {
			Log.debug(LogLevel.VERBOSE, "MqttClient", "publish", "Publish message: " + new String(message) + " on the topic: " + topic);
			connection.publish(topic, message, Prtcl.DEFAUlT_QOS, retain);
			return true;
		} catch (Exception e) {
			Log.error("MqttClient", "publish", "Impossible to publish the message: " + message);
			Log.debug(LogLevel.VERBOSE, "MqttClient", "publish", e.getMessage());
			return false;
		}
	}
}
