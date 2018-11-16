package utils.client;

import gateway.MqttMessage;
import gateway.Sender;
import mqtt.MqttClient;
import mqtt.Topics;
import mqtt.sn.SnAction;
import utils.DeviceState;
import utils.Time;
import utils.address.Address16;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client extends Thread {

	private final long TIME_TO_WAIT_ACTION = 250; // milliseconds
	private final long TIME_TO_WAIT_MESSAGE = 5000; // milliseconds - 1 second
	private final short MAX_MESSAGES = 5;

	private boolean doAction = false;
	private SnAction action = null;

	private String name = "";
	private Integer duration = 0;
	private DeviceState state = DeviceState.DISCONNECTED;
	private MqttClient mqttClient = null;


	public Address64 address64 = null;
	public Address16 address16 = null;

	private final Sender sender;

	private final List<MqttMessage> mqttMessages = Collections.synchronizedList(new ArrayList<>());

	public static final Topics Topics = new Topics();

	public Client(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
	}

	public synchronized Boolean addMqttMessage(final MqttMessage message) {

		while (MAX_MESSAGES <= mqttMessages.size()) {
			mqttMessages.remove(0);
			Log.debug(LogLevel.VERBOSE, "Client", "addMqttMessage", "too many messages -> removing oldest message");
		}

		Log.debug(LogLevel.VERBOSE, "Client", "addMqttMessage", "save message");
		return mqttMessages.add(message);
	}

	public void sendMqttMessages() {

		synchronized (mqttMessages) {
			for (MqttMessage mqttMessage : mqttMessages) {

				Log.debug(LogLevel.ACTIVE, "Client", "sendMqttMessages", "sending mqttMessage " + mqttMessage);
				sender.send(mqttMessage);

				Log.debug(LogLevel.VERBOSE, "Client", "sendMqttMessages", "wait before sending next message");
				Time.sleep(TIME_TO_WAIT_MESSAGE, "Client.sendMqttMessages(): fail waiting between two messages");
			}
			Log.debug(LogLevel.VERBOSE, "Client", "sendMqttMessages", "all messages have been sent");
			mqttMessages.clear();
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

		for (MqttMessage message : mqttMessages) {
			if (message.messageId().equals(messageId)) {
				messageFound = true;
				break;
			}
			idMessageToDelete++;
		}

		if (messageFound) {
			synchronized (mqttMessages) {
				mqttMessages.remove(idMessageToDelete);
			}
			Log.debug(LogLevel.VERBOSE, "Client", "acquitMessage", "message " + messageId + " acquitted");
		}

		return messageFound;
	}

	public String name() {
		return name;
	}

	public Client setClientName(final String name) {

		if (null == name) {
			Log.error("Client", "setName", "name is null");
		}

		this.name = name;

		Log.debug(LogLevel.VERBOSE, "Client", "setName", "Register client's name with " + name);

		return this;
	}

	public MqttClient mqttClient() {
		return mqttClient;
	}

	public Client setMqttClient(final MqttClient mqttClient) {

		if (null == mqttClient) {
			Log.error("Client", "setMqttClient", "mqttClient is null");
		}

		this.mqttClient = mqttClient;

		Log.debug(LogLevel.VERBOSE, "Client", "setMqttClient", "Register client's mqttClient with " + mqttClient);

		return this;
	}

	public DeviceState state() {
		return state;
	}

	public Client setState(final DeviceState state) {

		if (null == state) {
			Log.error("Client", "setState", "state is null");
		}

		this.state = state;

		Log.debug(LogLevel.VERBOSE, "Client", "setState", "Register client's state with " + state);

		return this;
	}

	public Integer duration() {
		return duration;
	}

	public Client setDuration(final Integer duration) {

		if (null == duration) {
			Log.error("Client", "setDuration", "duration is null");
		}

		this.duration = duration;

		Log.debug(LogLevel.VERBOSE, "Client", "setDuration", "Register client's duration with " + duration);

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
			Time.sleep(TIME_TO_WAIT_ACTION, "Client.run(): fail to wait");
		}
	}

	private void resetAction() {
		doAction = false;
		action = null;
	}

	public Client setAction(SnAction action) {

		if (null == action) {
			Log.error("Client", "setAction", "action is null");
		}

		this.action = action;
		doAction = true;

		return this;
	}
}