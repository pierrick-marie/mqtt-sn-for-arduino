package mqtt;

import gateway.MqttMessage;
import gateway.Sender;
import org.fusesource.mqtt.client.*;
import utils.Time;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

public class MqttClient extends MQTT {

	public static final String HOST = "localhost";
	public static final Integer PORT = 1883;

	private final long TIME_TO_WAIT = 500; // 0.5 seconds
	public final short NB_TRY = 5;

	private final BlockingConnection connection;
	private Boolean isConnected = false;

	class ThreadConnect extends Thread {

		public Boolean isConnected;

		public ThreadConnect() {
			isConnected = false;
		}

		public void run() {
			try {
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", "starting a new connection to the mqtt broker");
				connection.connect();
				isConnected = true;
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnect", "run", "connection to the mqtt broker activated");
			} catch (Exception e) {
				Log.error("Inner class: ThreadConnect", "run", "connection to the mqtt broker impossible");
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
				connection.subscribe(new Topic[]{new Topic(topicName, QoS.AT_LEAST_ONCE)});
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

				// @TODO debug tests
				client.addMqttMessage(new MqttMessage("SUB_ObiOne-RFID", "Payload 0"));
				client.addMqttMessage(new MqttMessage("SUB_ObiOne-RFID", "Payload 1"));
				client.addMqttMessage(new MqttMessage("SUB_ObiOne-RFID", "Payload 2"));

				while(true) {
					message = connection.receive();
					String payload = new String(message.getPayload());
					MqttMessage mqttMessage = new MqttMessage(message.getTopic(), payload);

					message.ack();
					client.addMqttMessage(mqttMessage);

					Log.verboseDebug("Message: " + payload + " on topic: " + message.getTopic() + " have been received");
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

		Log.debug(LogLevel.ACTIVE, "MqttClient", "connect", "try to connect to the mqtt broker");

		ThreadConnect threadConnection = new ThreadConnect();
		threadConnection.start();
		long time = System.currentTimeMillis();

		while (time + NB_TRY * TIME_TO_WAIT > System.currentTimeMillis() && false == threadConnection.isConnected) {
			Time.sleep(TIME_TO_WAIT, "time out connection");
		}

		if (!threadConnection.isConnected) {
			Log.error("MqttClient", "connect", "time out - stop connection to mqtt server");
			threadConnection.stopConnection();
			TimeoutException e = new TimeoutException("impossible to reach the mqtt server");
			throw e;
		}

		Log.debug(LogLevel.ACTIVE, "MqttClient", "connect", "connected to the mqtt broker");
		isConnected = true;
	}

	public void subscribe(final Client client, final String topicName) throws TimeoutException {

		Log.debug(LogLevel.ACTIVE, "MqttClient", "subscribe", "try to subscribe to the topic: " + topicName);

		ThreadSubscribe threadSubscribe = new ThreadSubscribe(topicName);
		threadSubscribe.start();
		long time = System.currentTimeMillis();

		while (time + NB_TRY * TIME_TO_WAIT > System.currentTimeMillis() && false == threadSubscribe.isSubscribed) {
			Time.sleep(TIME_TO_WAIT, "time out connection");
		}

		if (!threadSubscribe.isSubscribed) {
			Log.error("MqttClient", "subscribe", "time out - stop subscription to mqtt server");
			threadSubscribe.stopSubscribe();
			TimeoutException e = new TimeoutException("impossible to subscribe " + topicName + " to the mqtt server");
			throw e;
		}

		Log.debug(LogLevel.ACTIVE, "MqttClient", "subscribe", "subscription to the mqtt broker OK");

		ThreadListenMessage threadListenMessage = new ThreadListenMessage(client);
		threadListenMessage.start();
	}

	public Boolean isConnected() {
		return isConnected;
	}

	public Boolean publish(final String topic, final byte[] message, final QoS qos, final Boolean retain) {
		try {
			connection.publish(topic, message, qos, retain);
			return true;
		} catch (Exception e) {
			Log.error("MqttClient", "publish", "Impossible to publish the message: " + message + " on the topic: " + topic);
			Log.debug(LogLevel.VERBOSE, "MqttClient", "publish", e.getMessage());
			return false;
		}
	}
}
