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

public class Client implements MqttCallback {

	private final static String PRTCL = "tcp://";
	private final short MAX_MESSAGES = 5;

	// NOT IMPLEMENTED YET
	// private final Boolean cleanSession;

	private final Device device;
	private final MqttClient mqttClient;
	private final MqttConnectOptions option;

	public Client(final Device device, final Boolean cleanSession) throws MqttException {
		mqttClient = new MqttClient(PRTCL + Config.IP_SERVER + ":" + Config.PORT_SERVER, device.name(),
				new MemoryPersistence());

		this.device = device;
		// this.cleanSession = cleanSession;
		option = new MqttConnectOptions();
		option.setCleanSession(cleanSession);
		mqttClient.setCallback(this);
	}

	private void addMqttMessage(final SnMessage message) {

		synchronized (device.Messages) {
			while (MAX_MESSAGES <= device.Messages.size()) {
				device.Messages.remove(0);
				Log.debug(LogLevel.VERBOSE, "Client", "addMqttMessage",
						"too many messages -> removing oldest message");
			}

			Log.debug(LogLevel.VERBOSE, "Client", "addMqttMessage", "save message");
			device.Messages.add(message);
		}
	}

	public Boolean connect() {

		Log.debug(LogLevel.VERBOSE, "Client", "connect", "try to connect to the gateway.mqtt broker");

		if (mqttClient.isConnected()) {
			return true;
		}

		try {
			mqttClient.connect();
		} catch (final MqttException e) {
			Log.error("Client", "connect", e.getMessage());
			Log.error("Client", "connect", e.getCause().getMessage());
		}

		Log.debug(LogLevel.VERBOSE, "Client", "connect", device.name() + " connected");
		return mqttClient.isConnected();
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
			Log.debug(LogLevel.ACTIVE, "Client", "deliveryComplete", "Error while delivering message");
		}
	}

	public Boolean disconnect() {

		try {
			mqttClient.disconnect();
			Log.debug(LogLevel.VERBOSE, "Client", "disconnect", device.name() + " disconnected");
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "disconnect", e.getMessage());
			Log.debug(LogLevel.ACTIVE, "Client", "disconnect", "Impossible to disconnect the client");
			return false;
		}
	}

	public Boolean isConnected() {
		return mqttClient.isConnected();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {

		if (message.getPayload().length < PAYLOAD_LENGTH) {
			Log.debug(LogLevel.VERBOSE, "Client", "messageArrived",
					"message: " + new String(message.getPayload()) + " on topic: " + topic);
			addMqttMessage(new SnMessage(topic, new String(message.getPayload())));
		} else {
			Log.error("Client", "messageArrived", "payload too long");
		}
	}

	public Boolean publish(final String topicName, final String message) {

		try {
			final MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttMessage.setQos(Prtcl.DEFAULT_QOS);
			mqttClient.publish(topicName, mqttMessage);
			Log.debug(LogLevel.VERBOSE, "Client", "publish",
					"Publish message: " + message + " on the topic: " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "publish", "Impossible to publish the message: " + message);
			Log.debug(LogLevel.VERBOSE, "Client", "publish", e.getMessage());
			return false;
		}
	}

	public Boolean subscribe(final String topicName) {

		try {
			mqttClient.subscribe(topicName, Prtcl.DEFAULT_QOS);
			Log.debug(LogLevel.VERBOSE, "Client", "subscribe", device.name() + " subscribed to " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "subscribe", e.getMessage());
			return false;
		}
	}

	public Boolean unsubscribe(final String topicName) {

		try {
			mqttClient.unsubscribe(topicName);
			Log.verboseDebug("Client", "unsubscribe", device.name() + " unsubscribed to topic " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "unsubscribe", e.getMessage());
			return false;
		}
	}
}
