package utils.client;

import gateway.MqttMessage;
import gateway.Sender;
import mqtt.sn.SnAction;
import utils.DeviceState;
import utils.Time;
import utils.address.Address16;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;
import mqtt.MqttClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client extends Thread {

	private final long TIME_TO_WAIT = 5000; // milliseconds
	private boolean doAction = false;
	private SnAction action = null;

	private String name = "";
	private Integer duration = 0;
	private DeviceState state = DeviceState.DISCONNECTED;
	private MqttClient mqttClient = null;
	private Boolean willTopicReq = false;
	private Boolean willTopicAck = false;
	private Boolean willMessageAck = false;
	private Boolean willMessageReq = false;

	public Address64 address64 = null;
	public Address16 address16 = null;

	private final  List<MqttMessage> mqttMessages = Collections.synchronizedList(new ArrayList<>());

	private final Sender sender;
	private final MessageResender resender;

	public Client(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = DeviceState.FIRSTCONNECT;
		sender = new Sender(this);
		resender = new MessageResender();
		resender.start();
	}

	public synchronized Boolean addMqttMessage(final MqttMessage message) {
		return mqttMessages.add(message);
	}

	class MessageResender extends Thread {

		public void run() {

			List<Integer> listMessageToDelete = new ArrayList<>();
			int i;

			while (true) {

				i = 0;
				for (MqttMessage mqttMessage : mqttMessages) {

					if (mqttMessage.needToResend()) {

						Log.debug(LogLevel.ACTIVE, "Client", "MessageResender", "re-sending message " + mqttMessage);

						sender.send(mqttMessage);
					} else if (mqttMessage.needToDelete()) {
						listMessageToDelete.add(i);
					}
					i++;
				}

				for (Integer idToDelete : listMessageToDelete) {

					Log.debug(LogLevel.VERBOSE, "Client", "MessageResender", "delete message " + mqttMessages.get(idToDelete));
					synchronized (mqttMessages) { mqttMessages.remove(idToDelete); }
				}

				listMessageToDelete.clear();

				Time.sleep(TIME_TO_WAIT, "Client.MessageResender: error between two resend action");
			}
		}
	}

	public void sendMqttMessages() {


		for (MqttMessage mqttMessage : mqttMessages) {
			if (mqttMessage.needToSend()) {
				Log.debug(LogLevel.ACTIVE, "Client", "sendMqttMessages", "sending mqttMessage " + mqttMessage);

				sender.send(mqttMessage);
			}
		}
	}

	public synchronized Boolean acquitMessage(final Integer messageId) {


		for (MqttMessage message : mqttMessages) {
			if (message.messageId().equals(messageId)) {
				Log.debug(LogLevel.VERBOSE, "Client", "acquitMessage", "message " + messageId + " acquitted");
				message.acquitted();

				return true;
			}
		}

		return false;
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

	public Boolean willTopicReq() {
		return willTopicReq;
	}

	public Client setWillTopicReq(final Boolean willTopicReq) {

		if (null == willTopicReq) {
			Log.error("Client", "setWillTopicReq", "willTopicReq is null");
		}

		this.willTopicReq = willTopicReq;

		Log.debug(LogLevel.VERBOSE, "Client", "setWillTopicReq", "Register client's willTopicReq with " + willTopicReq);

		return this;
	}

	public Boolean willTopicAck() {
		return willTopicAck;
	}

	public Client setWillTopicAck(final Boolean willTopicAck) {

		if (null == willTopicAck) {
			Log.error("Client", "setWillTopicAck", "willTopicAck is null");
		}

		this.willTopicAck = willTopicAck;

		Log.debug(LogLevel.VERBOSE, "Client", "setWillTopicAck", "Register client's willTopicAck with " + willTopicAck);

		return this;
	}

	public Boolean willMessageAck() {
		return willMessageAck;
	}

	public Client setWillMessageAck(final Boolean willMessageAck) {

		if (null == willMessageAck) {
			Log.error("Client", "setWillMessageAck", "willMessageAck is null");
		}

		this.willMessageAck = willMessageAck;

		Log.debug(LogLevel.VERBOSE, "Client", "setWillMessageAck", "Register client's willMessageAck with " + willMessageAck);

		return this;
	}

	public Boolean willMessageReq() {
		return willMessageReq;
	}

	public Client setWillMessageReq(final Boolean willMessageReq) {

		if (null == willMessageReq) {
			Log.error("Client", "setWillMessageReq", "willMessageReq is null");
		}

		this.willMessageReq = willMessageReq;

		Log.debug(LogLevel.VERBOSE, "Client", "setWillMessageReq", "Register client's willMessageReq with " + willMessageReq);

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
			Time.sleep(TIME_TO_WAIT, "Client.run(): fail to wait");
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