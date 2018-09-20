package utils.mqttclient;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.FutureConnection;
import utils.log.Log;

import java.util.concurrent.TimeoutException;

public class MqttConnection extends BlockingConnection {

	private final long TIME_TO_WAIT = 1000; // 1 seconds

	class ThreadConnection extends Thread {

		public Boolean isConnected;
		private MqttConnection connection;

		public ThreadConnection(final MqttConnection connection) {
			isConnected = false;
			this.connection = connection;
		}

		public void run() {
			try {
				connection.connect();
				isConnected = true;
			} catch (Exception e) {
				Log.acticeDebug(e.getMessage());
			}
		}

		public synchronized void stopConnection() {
			Log.acticeDebug("stop connection");
			Thread.currentThread().interrupt();
		}
	}


	public MqttConnection(FutureConnection next) {
		super(next);
	}

	public void timeOutConnect(final long timeToWait) throws TimeoutException {
		Log.acticeDebug("MqttConnection - connect");

		long time = System.currentTimeMillis();

		ThreadConnection connection = new ThreadConnection(this);
		connection.start();

		while(time + timeToWait > System.currentTimeMillis() && false == connection.isConnected) {
			try {
				Thread.sleep(TIME_TO_WAIT);
				Log.acticeDebug("start to wait for a second " + time + " - " + System.currentTimeMillis());
			} catch (InterruptedException e) {
				Log.acticeDebug(e.getMessage());
			}
		}

		if(!connection.isConnected) {
			connection.stopConnection();
			Log.acticeDebug("Time out connection to mqtt server");
			TimeoutException e = new TimeoutException("TimeOutException: impossible to get the mqtt server");
			throw e;
		}
	}
}

