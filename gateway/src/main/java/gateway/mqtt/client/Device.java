/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.mqtt.impl.Client;
import gateway.mqtt.impl.MqMessage;
import gateway.mqtt.impl.Sender;
import gateway.mqtt.impl.Topic;
import gateway.mqtt.sn.IAction;
import gateway.utils.Time;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Device extends Thread {

	private final long TIME_TO_WAIT_ACTION = 250; // milliseconds
	private final long TIME_TO_WAIT_MESSAGE = 5000; // milliseconds - 1 second
	private final short MAX_MESSAGES = 5;

	private boolean doAction = false;
	private IAction action = null;

	private Integer duration = 0;
	private DeviceState state = DeviceState.DISCONNECTED;
	private Client mqttClient = null;

	private final Address64 address64;
	private final Address16 address16;
	private final Sender sender;

	private final List<MqMessage> Messages = Collections.synchronizedList(new ArrayList<>());
	private final List<Topic> Topics = Collections.synchronizedList(new ArrayList<>());

	public Device(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
	}

	public synchronized Boolean addMqttMessage(final MqMessage message) {

		while (MAX_MESSAGES <= Messages.size()) {
			Messages.remove(0);
			Log.debug(LogLevel.VERBOSE, "Device", "addMqttMessage",
					"too many messages -> removing oldest message");
		}

		Log.debug(LogLevel.VERBOSE, "Device", "addMqttMessage", "save message");
		return Messages.add(message);
	}

	public Address16 address16() {
		return address16;
	}

	public Address64 address64() {
		return address64;
	}

	synchronized public Topic addTopic(final String name) {

		final Topic ret = new Topic(Topics.size(), name);
		Topics.add(ret);

		return ret;
	}

	public Boolean connect() {
		if (null == mqttClient) {
			Log.error("Device", "connect", "mqtt client is null");
		}
		return mqttClient.doConnect();
	}

	synchronized public Boolean containsTopic(final Integer id) {

		for (final Topic topic : Topics) {
			if (topic.id().equals(id)) {
				return true;
			}
		}

		return false;
	}

	synchronized public Boolean containsTopic(final String name) {

		for (final Topic topic : Topics) {
			if (topic.name().toString().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public Integer duration() {
		return duration;
	}

	synchronized public Topic getTopic(final Integer id) {

		for (final Topic topic : Topics) {
			if (topic.id().equals(id)) {
				return topic;
			}
		}

		return null;
	}

	synchronized public Topic getTopic(final String name) {

		for (final Topic topic : Topics) {
			if (topic.name().toString().equals(name)) {
				return topic;
			}
		}

		return null;
	}

	public Device initMqttClient(final Boolean cleanSeassion) {
		try {
			mqttClient = new Client(this, cleanSeassion);
		} catch (final MqttException e) {
			Log.error("Device", "initMqttClient", "Error while creating the Mqtt client");
			Log.verboseDebug(e.getMessage());
			Log.verboseDebug(e.getCause().getMessage());
		}

		return this;
	}

	public Boolean isConnected() {
		return mqttClient.isConnected();
	}

	public Integer nbTopics() {
		return Topics.size();
	}

	public Boolean publish(final Topic topic, final String message) {
		return mqttClient.publish(topic, message);
	}

	private void resetAction() {
		doAction = false;
		action = null;
	}

	@Override
	public void run() {

		while (true) {
			if (doAction) {
				action.exec();
				resetAction();
			}
			Time.sleep(TIME_TO_WAIT_ACTION, "Device.run(): fail to wait");
		}
	}

	public void sendMqttMessages() {

		synchronized (Messages) {
			for (final MqMessage message : Messages) {

				Log.debug(LogLevel.ACTIVE, "Device", "sendMqttMessages",
						"sending DMqMessage " + message.toString());
				sender.send(message);

				Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "wait before sending next message");
				Time.sleep(TIME_TO_WAIT_MESSAGE,
						"Device.sendMqttMessages(): fail waiting between two messages");
			}
			Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "all messages have been sent");
			Messages.clear();
		}
	}

	public Device setAction(IAction action) {

		if (null == action) {
			Log.error("Device", "setAction", "action is null");
		}

		this.action = action;
		doAction = true;

		return this;
	}

	public Device setDuration(final Integer duration) {

		if (null == duration) {
			Log.error("Device", "setDuration", "duration is null");
		}

		this.duration = duration;

		Log.debug(LogLevel.VERBOSE, "Device", "setDuration", "Register client's duration with " + duration);

		return this;
	}

	public Device setState(final DeviceState state) {

		if (null == state) {
			Log.error("Device", "setState", "state is null");
		}

		this.state = state;

		Log.debug(LogLevel.VERBOSE, "Device", "setState", "Register client's state with " + state);

		return this;
	}

	public DeviceState state() {
		return state;
	}

	public Boolean subscribe(final Topic topic) {
		if (mqttClient.subscribe(topic)) {
			topic.setSubscribed();
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return getName() + " (" + address64.toString() + ")";
	}

	private synchronized Boolean unscribeAll() {
		for (final Topic topic : Topics) {
			try {
				mqttClient.unsubscribe(topic.name());
			} catch (final MqttException e) {
				Log.error("Device", "unsubscribeAll", "Error while unscribe topic " + topic.name());
				Log.verboseDebug(e.getMessage());
				Log.verboseDebug(e.getCause().getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * @TODO not implemented yet Acquittals are only used with QoS level 1 and 2.
	 *       This feature is not used in the current implementation of the client.
	 *
	 * @param messageId the id of the acquitted message
	 * @return true if the message have been correctly acquitted
	 *
	 *         public synchronized Boolean acquitMessage(final Integer messageId) {
	 *
	 *         int idMessageToDelete = 0; boolean messageFound = false;
	 *
	 *         for (final MqMessage message : Messages) { if
	 *         (messageId.equals(message.getId())) { messageFound = true; break; }
	 *         idMessageToDelete++; }
	 *
	 *         if (messageFound) { synchronized (Messages) {
	 *         Messages.remove(idMessageToDelete); } Log.debug(LogLevel.VERBOSE,
	 *         "Device", "acquitMessage", "message " + messageId + " acquitted"); }
	 *
	 *         return messageFound; }
	 */
}