package utils.mqttclient;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.Time;
import utils.log.Log;
import utils.log.LogLevel;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

public class MqttClient extends MQTT {

	public static final String HOST = "localhost";
	public static final Integer PORT = 1883;

	private final long TIME_TO_WAIT = 1000; // 1 seconds

	private BlockingConnection connection = null;

	class ThreadConnection extends Thread {

		public Boolean isConnected;
		private BlockingConnection connection;

		public ThreadConnection(final BlockingConnection connection) {
			isConnected = false;
			this.connection = connection;
		}

		public void run() {
			try {
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnection", "run", "starting a new connection to the mqtt broker");
				connection.connect();
				isConnected = true;
				Log.debug(LogLevel.VERBOSE, "Inner class: ThreadConnection", "run", "connection to the mqtt broker activated");
			} catch (Exception e) {
				Log.error("Inner class: ThreadConnection", "run", "connection to the mqtt broker impossible");
				Log.debug(LogLevel.VERBOSE,"Inner class: ThreadConnection", "run", e.getMessage());
			}
		}

		public synchronized void stopConnection() {
			Log.debug(LogLevel.VERBOSE, "MqttClient", "stopConnection", "stopping the current thread");
			Thread.currentThread().interrupt();
		}
	}

	public  MqttClient() {
		try {
			setHost(HOST, PORT);
		} catch (URISyntaxException e) {
			Log.error("MqttClient", "constructor", "Impossible to set host: " + HOST + ":" + PORT);
			Log.debug(LogLevel.VERBOSE, "MqttClient", "constructor", e.getMessage());
		}
	}

	public BlockingConnection connect(final long timeToWait) throws TimeoutException {

		Log.debug(LogLevel.VERBOSE,"MqttClient", "connect","try to connect to the mqtt broker");

		connection = this.blockingConnection();
		ThreadConnection threadConnection = new ThreadConnection(connection);
		threadConnection.start();
		long time = System.currentTimeMillis();

		while(time + timeToWait > System.currentTimeMillis() && false == threadConnection.isConnected) {
			Time.sleep(TIME_TO_WAIT, "time out connection");
		}

		if(!threadConnection.isConnected) {
			Log.error("MqttClient", "connect", "time out - stop connection to mqtt server");
			threadConnection.stopConnection();
			TimeoutException e = new TimeoutException("impossible to reach the mqtt server");
			throw e;
		} else {
			Log.debug(LogLevel.VERBOSE,"MqttClient", "connect","connected to the mqtt broker");
		}

		return connection;
	}

	public BlockingConnection connection() {
		Log.debug(LogLevel.VERBOSE,"MqttClient", "connection","get current blocking connection");
		return connection;
	}
}
