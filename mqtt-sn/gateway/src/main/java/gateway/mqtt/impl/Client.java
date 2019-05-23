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
				Log.debug("Client", "addMqttMessage", "too many messages -> removing oldest message");
			}

			Log.debug("Client", "addMqttMessage", "save message");
			device.Messages.add(message);
		}
	}

	public Boolean connect() {

		Log.debug("Client", "connect", "try to connect to the broker");

		if (mqttClient.isConnected()) {
			return true;
		}

		try {
			mqttClient.connect();
		} catch (final MqttException e) {
			Log.error("Client", "connect",
					"Can' access to the broker " + PRTCL + Config.IP_SERVER + ":" + Config.PORT_SERVER);
			Log.debug("Client", "connect",
					"org.eclipse.paho.client.mqttv3.MqttClient error code " + e.getReasonCode());
			Log.debug("Abording");
			System.exit(-2);
		}

		Log.info(device + " connected to the broker");
		return mqttClient.isConnected();
	}

	@Override
	public void connectionLost(Throwable throwable) {
		Log.error("Client", "connectionLost", throwable.getMessage());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

		try {
			Log.debug("Client", "deliveryComplete", iMqttDeliveryToken.getMessage().toString());
		} catch (final MqttException e) {
			Log.error("Client", "deliveryComplete", "Error while delivering message");
			Log.debug("Client", "deliveryComplete",
					"org.eclipse.paho.client.mqttv3.MqttException error code " + e.getReasonCode());
		}
	}

	public Boolean disconnect() {

		try {
			mqttClient.disconnect();
			Log.debug("Client", "disconnect", device.name() + " disconnected");
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "disconnect", "Impossible to disconnect the client");
			Log.debug("Client", "disconnect",
					"org.eclipse.paho.client.mqttv3.MqttException error code " + e.getReasonCode());
			return false;
		}
	}

	public Boolean isConnected() {
		return mqttClient.isConnected();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {

		if (message.getPayload().length < PAYLOAD_LENGTH) {

			Log.brokerInput(device, new String(message.getPayload()) + " on topic " + topic);

			addMqttMessage(new SnMessage(topic, new String(message.getPayload())));
		} else {
			Log.error("Client", "messageArrived", "payload too long " + new String(message.getPayload()));
		}
	}

	public Boolean publish(final String topicName, final String message) {

		try {
			final MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttMessage.setQos(Prtcl.DEFAULT_QOS);
			mqttClient.publish(topicName, mqttMessage);
			Log.brokerOutput(device, message + " on topic " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "publish", "Impossible to publish the message: " + message);
			Log.debug("Client", "publish",
					"org.eclipse.paho.client.mqttv3.MqttException error code " + e.getReasonCode());
			return false;
		}
	}

	public Boolean subscribe(final String topicName) {

		try {
			mqttClient.subscribe(topicName, Prtcl.DEFAULT_QOS);
			Log.debug("Client", "subscribe", device.name() + " subscribed to " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "subscribe", "Impossible to subscribe topic " + topicName);
			Log.debug("Client", "subscrible",
					"org.eclipse.paho.client.mqttv3.MqttException error code " + e.getReasonCode());
			return false;
		}
	}

	public Boolean unsubscribe(final String topicName) {

		try {
			mqttClient.unsubscribe(topicName);
			Log.debug("Client", "unsubscribe", device.name() + " unsubscribed to topic " + topicName);
			return true;
		} catch (final MqttException e) {
			Log.error("Client", "unsubscribe", "Impossible to unsubscribe topic " + topicName);
			Log.debug("Client", "unsubscrible",
					"org.eclipse.paho.client.mqttv3.MqttException error code " + e.getReasonCode());
			return false;
		}
	}
}
