package utils.client;

import gateway.MqttMessage;
import utils.State;
import utils.address.Address16;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;
import mqtt.MqttClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {

	private String name = "";
	private Integer duration = 0;
	private State state = State.DISCONNECTED;
	private MqttClient mqttClient = null;
	private Boolean willTopicReq = false;
	private Boolean willTopicAck = false;
	private Boolean willMessageAck = false;
	private Boolean willMessageReq = false;

	public Address64 address64 = null;
	public Address16 address16 = null;

	private final List<MqttMessage> mqttMessages = Collections.synchronizedList(new ArrayList<>());

	public Client(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = State.FIRSTCONNECT;
	}

	public List<MqttMessage> mqttMessages() {
		return mqttMessages;
	}

	public synchronized Boolean addMqttMessage(final MqttMessage message) {
		Boolean ret = mqttMessages.add(message);

		return ret;
	}

	public synchronized Boolean removeMqttMessage(final int messageId) {
		Boolean ret = false;

		if( null != mqttMessages.remove(messageId) ){
			ret = true;
		}

		return ret;
	}

	public synchronized Boolean acquitMessage(final Integer messageId) {
		for(MqttMessage message : mqttMessages) {
			if(message.messageId().equals(messageId)) {
				message.setAcquitted(true);

				return true;
			}
		}

		return false;
	}

	public String name() {
		return name;
	}

	public Client setName(final String name) {

		if (null == name) {
			Log.error("Client", "setName", "name is null");
		}

		this.name = name;

		Log.debug(LogLevel.VERBOSE,"Client", "setName", "Register client's name with " + name);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setMqttClient", "Register client's mqttClient with " + mqttClient);

		return this;
	}

	public State state() {
		return state;
	}

	public Client setState(final State state) {

		if (null == state) {
			Log.error("Client", "setState", "state is null");
		}

		this.state = state;

		Log.debug(LogLevel.VERBOSE,"Client", "setState", "Register client's state with " + state);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setDuration", "Register client's duration with " + duration);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillTopicReq", "Register client's willTopicReq with " + willTopicReq);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillTopicAck", "Register client's willTopicAck with " + willTopicAck);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillMessageAck", "Register client's willMessageAck with " + willMessageAck);

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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillMessageReq", "Register client's willMessageReq with " + willMessageReq);

		return this;
	}

	public Boolean isSaved() {
		return null != Clients.list.search(address64, address16);
	}

	public String toString() {
		if( "" == name ) {
			return address64.toString();
		} else {
			return name + " (" + address64.toString() + ")";
		}
	}
}