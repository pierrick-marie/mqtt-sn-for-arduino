/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.impl;

import static gateway.mqtt.sn.impl.Prtcl.PAYLOAD_LENGTH;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import gateway.mqtt.client.Device;
import gateway.mqtt.sn.impl.Prtcl;
import gateway.utils.Config;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/*
 * @TODO remove extends parameter and add a real MqttClient attribute
 */

public class Client extends MqttClient implements MqttCallback { // , Runnable {

	private final static String PRTCL = "tcp://";
	private final Integer SLEEP_TIME = 5; // seconds

	private final Device device;
	// NOT IMPLEMENTED YET
	// private final Boolean cleanSession;

	private final Boolean isStarted = false;
	// private MqttClient mqttClient;
	private final MqttConnectOptions option;

	public Client(final Device device, final Boolean cleanSession) throws MqttException {
		super(PRTCL + Config.IP_SERVER + ":" + Config.PORT_SERVER, device.getName(), new MemoryPersistence());

		this.device = device;
		// this.cleanSession = cleanSession;
		option = new MqttConnectOptions();
		option.setCleanSession(cleanSession);

		super.setCallback(this);
	}

	@Override
	public void connectionLost(Throwable throwable) {

		Log.error("Client", "connectionLost", throwable.getMessage());

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

		try {
			Log.debug(LogLevel.VERBOSE, "Client", "deliveryComplete", iMqttDeliveryToken.getMessage().toString());
		} catch (final MqttException e) {
			Log.error("Client", "deliveryComplete", e.getMessage());
		}
	}

	public Boolean doConnect() {

		Log.debug(LogLevel.VERBOSE, "Client", "connect", "try to connect to the gateway.mqtt broker");

		if (super.isConnected()) {
			return true;
		}

		try {
			super.connect();
		} catch (final MqttException e) {
			Log.error("Client", "connect", e.getMessage());
			Log.error("Client", "connect", e.getCause().getMessage());
		}

		Log.debug(LogLevel.VERBOSE, "Client", "connect", device.getName() + " connected");
		return super.isConnected();
	}

	public Boolean doDisconnect() {

		/*
		 * Do nothing (stay connected), otherwise the device will not receive any
		 * message.
		 *
		 * isConnected = false; try { mqttClient.disconnect(); } catch (MqttException e)
		 * { return false; }
		 */

		Log.debug(LogLevel.VERBOSE, "Client", "disconnect", device.getName() + " disconnected");

		return true;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {

		if (message.getPayload().length < PAYLOAD_LENGTH) {
			Log.debug(LogLevel.VERBOSE, "Client", "messageArrived",
					"message: " + new String(message.getPayload()) + " on topic: " + topic);
			device.addMqttMessage(new SnMessage(topic, new String(message.getPayload())));
		} else {
			Log.error("Client", "messageArrived", "payload too long");
		}
	}

	public Boolean publish(Topic topic, String message) {

		try {
			final MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttMessage.setQos(Prtcl.DEFAULT_QOS);
			super.publish(topic.name(), mqttMessage);
			Log.debug(LogLevel.VERBOSE, "Client", "publish",
					"Publish message: " + message + " on the topic: " + topic);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "publish", "Impossible to publish the message: " + message);
			Log.debug(LogLevel.VERBOSE, "Client", "publish", e.getMessage());
			return false;
		}
	}

	@Override
	public Boolean subscribe(final String topicName) {

		/*
		 * if (!isStarted) { new Thread(this).start(); }
		 */

		try {
			super.subscribe(topicName);
			super.setCallback(this);
			Log.debug(LogLevel.VERBOSE, "Client", "subscribe", device.getName() + " subscribed to " + topicName);
		} catch (final MqttException e) {
			Log.error("Client", "subscribe", e.getMessage());
		}
	}
}
