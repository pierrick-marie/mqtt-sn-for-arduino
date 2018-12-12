/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.mqtt.impl.Client;
import gateway.mqtt.impl.Sender;
import gateway.mqtt.impl.SnMessage;
import gateway.mqtt.impl.Topic;
import gateway.mqtt.sn.IAction;
import gateway.mqtt.sn.impl.Prtcl;
import gateway.utils.Time;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Device extends Thread {

	private final long WAIT_NEXT_ACTION = 1000; // milliseconds - 1 second
	private final long WAIT_SENDING_NEXT_MESSAGE = 5000; // milliseconds - 5 second
	private final short MAX_MESSAGES = 5;

	private boolean doAction = false;
	private IAction action = null;

	private long lastUpdate = new Date().getTime();
	private long duration = 60l; // 60 seconds
	private DeviceState state = DeviceState.DISCONNECTED;
	private Client mqttClient = null;

	private final Address64 address64;
	private final Address16 address16;
	private final Sender sender;

	private final List<SnMessage> Messages = Collections.synchronizedList(new ArrayList<>());
	private final List<Topic> Topics = Collections.synchronizedList(new ArrayList<>());

	public Device(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
	}

	public synchronized Boolean addMqttMessage(final SnMessage message) {

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

	public Boolean connect(final int duration) {
		if (null == mqttClient) {
			Log.error("Device", "connect", "mqtt client is null");
		}
		Log.print(this + "Connected with a keep alive: " + duration);
		this.duration = duration;

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

	public long duration() {
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

	synchronized private Topic getTopic(final String name) {

		for (final Topic topic : Topics) {
			if (topic.name().toString().equals(name)) {
				return topic;
			}
		}

		return null;
	}

	synchronized public Integer getTopicId(final String topicName) {

		for (final Topic topic : Topics) {
			if (topic.name().toString().equals(topicName)) {
				return topic.id();
			}
		}

		Log.error("Device", "getTopicId", "topic id " + topicName + " not found!");
		return -1;
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

	public Topic register(final String topicName) {

		Topic ret = getTopic(topicName);
		if (null == ret) {
			ret = new Topic(Topics.size(), topicName);
			if (!Topics.add(ret)) {
				Log.debug(LogLevel.ACTIVE, "Device", "register", "Error during register topic");
				return null;
			}
		}

		Log.print(this + " Registered topic: " + topicName);
		return ret.setRegistered();
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
			// if the current date is upper than the last update + duration time
			if (!Topics.isEmpty() && new Date().getTime() > lastUpdate + duration * 1000) {
				unscribeAll();
			}
			Time.sleep(WAIT_NEXT_ACTION, "Device.run(): fail waiting next action");
		}
	}

	public void sendMqttMessages() {

		synchronized (Messages) {
			for (final SnMessage message : Messages) {

				Log.debug(LogLevel.ACTIVE, "Device", "sendMqttMessages",
						"sending DMqMessage " + message.toString());
				sender.send(message);

				Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "wait before sending next message");
				Time.sleep(WAIT_SENDING_NEXT_MESSAGE,
						"Device.sendMqttMessages(): fail waiting before sending next message");
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

	public Device setDuration(final Short duration) {

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

	public Topic subscribe(final String topicName) {

		Topic ret = getTopic(topicName);
		if (null == ret) {
			ret = new Topic(Topics.size(), topicName);
			if (!Topics.add(ret)) {
				Log.debug(LogLevel.ACTIVE, "Device", "subscribe", "Error during subscribe topic");
				return null;
			}
		}

		if (mqttClient.subscribe(topicName, Prtcl.DEFAULT_QOS)) {
			Log.debug(LogLevel.ACTIVE, "Device", "register", "Mqtt client - error during register topic");
			return null;
		}

		Log.print(this + " Subscribed topic: " + topicName);
		return ret.setRegistered();
	}

	@Override
	public String toString() {
		return getName() + " (" + address64.toString() + ")";
	}

	private synchronized void unscribeAll() {

		Log.print(this + " Time out: unsubscibe all topics");

		for (final Topic topic : Topics) {
			try {
				/*
				 * @TODO create a list of all topic name to unsubscribe and unsubscribe all in
				 * one method call
				 */
				mqttClient.unsubscribe(topic.name());
			} catch (final MqttException e) {
				Log.error("Device", " unsubscribeAll", "Error while unscribe topic " + topic.name());
				Log.verboseDebug(e.getMessage());
				Log.verboseDebug(e.getCause().getMessage());
			}
		}
		/*
		 * @TODO change to an attribute that indicates whether subscribed topics are
		 * unsubscribed or not
		 */
		Topics.clear();
	}

	public void updateTimer() {

		lastUpdate = new Date().getTime();
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