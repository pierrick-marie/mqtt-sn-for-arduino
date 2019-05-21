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
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Device extends Thread {

	private final long WAIT_NEXT_ACTION = 1000; // milliseconds - 1 second
	private final long WAIT_SENDING_NEXT_MESSAGE = 5000; // milliseconds - 5 second
	private final short MAX_MESSAGES = 5;

	private final Sender sender;

	private volatile boolean doAction = false;
	private boolean doUnsubscribeAll = false;
	private IAction action = null;

	private Address64 address64;
	private Address16 address16;
	private long lastUpdate = new Date().getTime();
	private long duration = 60l; // 60 seconds
	private DeviceState state = DeviceState.DISCONNECTED;
	private Client mqttClient = null;

	private final List<SnMessage> Messages = Collections.synchronizedList(new ArrayList<>());
	private final List<Topic> Topics = Collections.synchronizedList(new ArrayList<>());

	public Device(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
	}

	synchronized public Boolean addMqttMessage(final SnMessage message) {

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

	synchronized public Boolean connect(final int duration) {
		if (null == mqttClient) {
			Log.error("Device", "connect", "mqtt client is null");
		}
		Log.print(this + " - connected with a keep alive: " + duration);
		this.duration = duration;

		return mqttClient.connect();
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

	synchronized public Device initMqttClient(final Boolean cleanSeassion) {
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

	synchronized public Boolean publish(final Topic topic, final String message) {
		return mqttClient.publish(topic.name(), message);
	}

	synchronized public Topic register(final String topicName) {

		Topic topic = getTopic(topicName);
		if (null == topic) {
			topic = new Topic(Topics.size(), topicName);
			if (!Topics.add(topic)) {
				Log.debug(LogLevel.ACTIVE, "Device", "register", "Error during register topic");
				return null;
			}
		}

		Log.print(this + " - registered topic: " + topicName);
		return topic.setRegistered(true);
	}

	synchronized public void resetAction() {
		doAction = false;
		action = null;
	}

	@Override
	public void run() {

		boolean run = true;

		while (run) {
			if (doAction) {
				action.exec();
				resetAction();
			}
			// if the current date is upper than the last update + duration time
			if (doUnsubscribeAll && new Date().getTime() > lastUpdate + duration * 1000) {
				Log.print(this + " - timeout: unsubscribe topics");

				unsubscribeAll();
			}
			// if the current date is upper than the last update + 3 x duration time
			if (new Date().getTime() > lastUpdate + duration * 3000) {
				Log.print(this + " - timeout: remove device");

				Topics.clear();
				address16 = null;
				address64 = null;
				duration = 0;
				if (null != mqttClient) {
					mqttClient.disconnect();
				}
				Devices.list.remove(this);
				setName("TO-REMOVE!");
				run = false;
			}

			try {
				sleep(WAIT_NEXT_ACTION);
			} catch (final Exception e) {
				Log.error("Device", "run", "fail waiting next action");
				Log.debug(LogLevel.VERBOSE, "Device", "run", e.getMessage());
			}
		}
	}

	/**
	 * The function is call after a PingReq. If it returns "false", pingreq have to
	 * stop the rest of its instructions -> the device have been reset.
	 *
	 * @return false if the device have been reset, true otherwise.
	 */
	synchronized public Boolean sendMqttMessages() {

		for (final SnMessage message : Messages) {

			/*
			 * While doAction is true, send messages. Sometimes the device is reset (new
			 * connection @see RawDataParser switch case MessageType.SEARCHGW) while it's
			 * waiting to send the next message. In that case, we have to quit the function.
			 */
			if (doAction) {
				Log.debug(LogLevel.ACTIVE, "Device", "sendMqttMessages",
						"sending message for topic: " + message.topic());
				sender.send(message);

				Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "wait before sending next message");
				try {
					sleep(WAIT_SENDING_NEXT_MESSAGE);
				} catch (final Exception e) {
					Log.error("Device", "sendMqttMessages", "fail waiting before sending next message");
					Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", e.getMessage());
					return false;
				}
			} else {
				return false;
			}
		}
		Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "all messages have been sent");
		Messages.clear();

		return doAction;
	}

	synchronized public Device setAction(IAction action) {

		if (null == action) {
			Log.error("Device", "setAction", "action is null");
		}

		this.action = action;
		doAction = true;

		return this;
	}

	synchronized public Device setDuration(final Short duration) {

		if (null == duration) {
			Log.error("Device", "setDuration", "duration is null");
		}

		this.duration = duration;

		Log.debug(LogLevel.VERBOSE, "Device", "setDuration", "Register client's duration with " + duration);

		return this;
	}

	synchronized public Device setState(final DeviceState state) {

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

	synchronized public Topic subscribe(final String topicName) {

		Topic topic = getTopic(topicName);
		if (null == topic) {
			topic = new Topic(Topics.size(), topicName);
			if (!Topics.add(topic)) {
				Log.debug(LogLevel.ACTIVE, "Device", "subscribe", "Error during subscribe topic");
				return null;
			}
		}

		if (!mqttClient.subscribe(topicName)) {
			Log.debug(LogLevel.ACTIVE, "Device", "subscribe", "Mqtt client - error during register topic");
			return null;
		}

		Log.print(this + " - subscribed to " + topicName + " with id " + topic.id());
		doUnsubscribeAll = true;
		return topic.setSubscribed(true);
	}

	@Override
	public String toString() {

		if (getName().startsWith("Thread")) {
			return address64.toString();
		} else {
			return getName();
		}
	}

	synchronized private void unsubscribeAll() {

		Log.print(this + " Time out: unsubscibe all topics");

		for (final Topic topic : Topics) {

			Log.error("Device", "unssubscribeAll",
					topic.name() + " - " + topic.id() + " - " + topic.isSubscribed());

			if (topic.isSubscribed()) {
				mqttClient.unsubscribe(topic.name());
				topic.setRegistered(false);
			}
		}
		doUnsubscribeAll = false;
	}

	synchronized public void updateTimer() {

		Log.debug(LogLevel.VERBOSE, "Device", "updateTimer", "");
		lastUpdate = new Date().getTime();
		Log.debug(LogLevel.VERBOSE, "Device", "updateTimer", "" + lastUpdate);
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