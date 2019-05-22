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
import gateway.mqtt.impl.SnMessage;
import gateway.mqtt.impl.Topic;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Device {

	private Address64 address64;
	private Address16 address16;
	private long lastUpdate = new Date().getTime();
	private long duration = 60l; // 60 seconds
	private DeviceState state = DeviceState.DISCONNECTED;
	private Client mqttClient = null;
	private String name;
	private Boolean run;
	private Thread currentThread;

	public final List<SnMessage> Messages = Collections.synchronizedList(new ArrayList<>());
	public final List<Topic> Topics = Collections.synchronizedList(new ArrayList<>());

	public Device(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		run = true;
		currentThread = new Thread();
		name = "Unnamed device";

		final Runnable runnable = () -> {
			while (run) {
				// if the current date is upper than the last update + 3 x duration time
				if (new Date().getTime() > lastUpdate + duration * 3000) {
					Log.print(this + " - timeout: remove device");
					removeDevice();
				}

				try {
					Thread.sleep(duration * 1000);
				} catch (final InterruptedException e) {
					Log.error("Device", "constructor", "fail waiting next action");
					Log.verboseDebug(e.getMessage());
				}
			}
		};
		new Thread(runnable).start();

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

	synchronized public void initMqttClient(final Boolean cleanSeassion) {
		try {
			mqttClient = new Client(this, cleanSeassion);
		} catch (final MqttException e) {
			Log.error("Device", "initMqttClient", "Error while creating the Mqtt client");
			Log.verboseDebug(e.getMessage());
			Log.verboseDebug(e.getCause().getMessage());
		}
	}

	public Boolean isConnected() {
		return mqttClient.isConnected();
	}

	public String name() {
		return name;
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

	private void removeDevice() {
		Topics.clear();
		address16 = null;
		address64 = null;
		duration = 0;
		if (null != mqttClient) {
			mqttClient.disconnect();
		}
		Devices.list.remove(this);
		run = false;
	}

	synchronized public void setAction(final Runnable action) {

		if (null == action) {
			Log.error("Device", "setAction", "action is null");
			return;
		}

		Log.debug(LogLevel.VERBOSE, "Device", "setAction", "setup " + action.getClass().getSimpleName());

		while (currentThread.isAlive()) {
			currentThread.interrupt();
		}

		currentThread = new Thread(action);
		currentThread.start();
	}

	synchronized public void setDuration(final Short duration) {

		if (null == duration) {
			Log.error("Device", "setDuration", "duration is null");
		}

		this.duration = duration;

		Log.debug(LogLevel.VERBOSE, "Device", "setDuration", "Register client's duration with " + duration);
	}

	public void setName(final String name) {
		this.name = name;
	}

	synchronized public void setState(final DeviceState state) {

		if (null == state) {
			Log.error("Device", "setState", "state is null");
		}

		this.state = state;

		Log.debug(LogLevel.VERBOSE, "Device", "setState", "Register client's state with " + state);
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

		final Runnable runnable = () -> {
			while (run) {
				// if the current date is upper than the last update + duration time
				if (!Topics.isEmpty() && new Date().getTime() > lastUpdate + duration * 1000) {
					unsubscribeAll();
				}

				try {
					Thread.sleep(duration * 1000);
				} catch (final InterruptedException e) {
					Log.error("Device", "subscribe", "fail waiting next action");
					Log.verboseDebug(e.getMessage());
				}
			}
		};
		new Thread(runnable).start();

		return topic.setSubscribed(true);
	}

	@Override
	public String toString() {

		if (name().startsWith("Thread")) {
			return address64.toString();
		} else {
			return name();
		}
	}

	synchronized private void unsubscribeAll() {

		Log.print(this + " Time out: unsubscibe all topics");

		for (final Topic topic : Topics) {

			Log.verboseDebug("Device", "unsubscribeAll", topic.name() + " id: " + topic.id());

			if (topic.isSubscribed()) {
				mqttClient.unsubscribe(topic.name());
				topic.setRegistered(false);
			}
		}
		Topics.clear();
	}

	synchronized public void updateTimer() {

		final Date d = new Date();
		lastUpdate = d.getTime();

		Log.debug(LogLevel.VERBOSE, "Device", "updateTimer", "" + d);
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