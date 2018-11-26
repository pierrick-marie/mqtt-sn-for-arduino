package gateway.mqtt.client;

import gateway.mqtt.*;
import gateway.mqtt.sn.SnAction;
import gateway.utils.Time;
import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Device extends Thread {

	private final long TIME_TO_WAIT_ACTION = 250; // milliseconds
	private final long TIME_TO_WAIT_MESSAGE = 5000; // milliseconds - 1 second
	private final short MAX_MESSAGES = 5;

	private boolean doAction = false;
	private SnAction action = null;

	private String name = "";
	private Integer duration = 0;
	private DeviceState state = DeviceState.DISCONNECTED;
	private Client mqttClient = null;


	public Address64 address64 = null;
	public Address16 address16 = null;

	private final Sender sender;

	private final List<MqMessage> messages = Collections.synchronizedList(new ArrayList<>());

	public static final Topics Topics = new Topics();

	public Device(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
	}

	public synchronized Boolean addMqttMessage(final MqMessage message) {

		while (MAX_MESSAGES <= messages.size()) {
			messages.remove(0);
			Log.debug(LogLevel.VERBOSE, "Device", "addMqttMessage", "too many messages -> removing oldest message");
		}

		Log.debug(LogLevel.VERBOSE, "Device", "addMqttMessage", "save message");
		return messages.add(message);
	}

	public void sendMqttMessages() {

		synchronized (messages) {
			for (MqMessage message : messages) {

				Log.debug(LogLevel.ACTIVE, "Device", "sendMqttMessages", "sending DMqMessage " + message.toString());
				sender.send(message);

				Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "wait before sending next message");
				Time.sleep(TIME_TO_WAIT_MESSAGE, "Device.sendMqttMessages(): fail waiting between two messages");
			}
			Log.debug(LogLevel.VERBOSE, "Device", "sendMqttMessages", "all messages have been sent");
			messages.clear();
		}
	}

	/**
	 * @TODO not implemented yet
	 * Acquittals are only used with QoS level 1 and 2. This feature is not used in the current implementation of the client.
	 *
	 * @param messageId the id of the acquitted message
	 * @return true if the message have been correctly acquitted
	 */
	public synchronized Boolean acquitMessage(final Integer messageId) {

		int idMessageToDelete = 0;
		boolean messageFound = false;

		for (MqMessage message : messages) {
			if (messageId.equals(message.getId())) {
				messageFound = true;
				break;
			}
			idMessageToDelete++;
		}

		if (messageFound) {
			synchronized (messages) {
				messages.remove(idMessageToDelete);
			}
			Log.debug(LogLevel.VERBOSE, "Device", "acquitMessage", "message " + messageId + " acquitted");
		}

		return messageFound;
	}

	public String name() {
		return name;
	}

	public Device setClientName(final String name) {

		if (null == name) {
			Log.error("Device", "setName", "name is null");
		}

		this.name = name;

		Log.debug(LogLevel.VERBOSE, "Device", "setName", "Register client's name with " + name);

		return this;
	}

	public Client mqttClient() {
		return mqttClient;
	}

	public Device setMqttClient(final Client mqttClient) {

		if (null == mqttClient) {
			Log.error("Device", "setMqttClient", "mqttClient is null");
		}

		this.mqttClient = mqttClient;

		Log.debug(LogLevel.VERBOSE, "Device", "setMqttClient", "Register client's mqttClient with " + mqttClient);

		return this;
	}

	public DeviceState state() {
		return state;
	}

	public Device setState(final DeviceState state) {

		if (null == state) {
			Log.error("Device", "setState", "state is null");
		}

		this.state = state;

		Log.debug(LogLevel.VERBOSE, "Device", "setState", "Register client's state with " + state);

		return this;
	}

	public Integer duration() {
		return duration;
	}

	public Device setDuration(final Integer duration) {

		if (null == duration) {
			Log.error("Device", "setDuration", "duration is null");
		}

		this.duration = duration;

		Log.debug(LogLevel.VERBOSE, "Device", "setDuration", "Register client's duration with " + duration);

		return this;
	}

	public String toString() {
		if ("" == name) {
			return address64.toString();
		} else {
			return name + " (" + address64.toString() + ")";
		}
	}

	public void run() {

		while (true) {
			if (doAction) {
				action.exec();
				resetAction();
			}
			Time.sleep(TIME_TO_WAIT_ACTION, "Device.run(): fail to wait");
		}
	}

	private void resetAction() {
		doAction = false;
		action = null;
	}

	public Device setAction(SnAction action) {

		if (null == action) {
			Log.error("Device", "setAction", "action is null");
		}

		this.action = action;
		doAction = true;

		return this;
	}
}