package utils;

import gateway.Message;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.address.Address16;
import utils.address.Address64;

import java.util.ArrayList;

public class Client {

	private String name = "";
	private Integer duration = 0;
	private State state = State.DISCONNECTED;
	private MQTT mqttClient = null;
	private Boolean willTopicReq = false;
	private Boolean willTopicAck = false;
	private Boolean willMessageAck = false;
	private Boolean willMessageReq = false;
	private CallbackConnection connection = null;

	public Address64 address64 = null;
	public Address16 address16 = null;

	public final ArrayList<Message> messages = new ArrayList<>();

	public Client(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
	}

	public CallbackConnection connection() {
		return connection;
	}

	public Client setConnection(final CallbackConnection connection) {

		if (null == connection) {
			Log.error("Client", "setConnection", "connection is null");
		}

		this.connection = connection;

		save();

		return this;
	}

	public String name() {
		return name;
	}

	public Client setName(final String name) {

		if (null == name) {
			Log.error("Client", "setName", "name is null");
		}

		this.name = name;

		save();

		return this;
	}

	public MQTT mqttClient() {
		return mqttClient;
	}

	public Client setMqttClient(final MQTT mqttClient) {

		if (null == mqttClient) {
			Log.error("Client", "setMqttClient", "mqttClient is null");
		}

		this.mqttClient = mqttClient;

		save();

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

		save();

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

		save();

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

		save();

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

		save();

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

		save();

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

		save();

		return this;
	}

	public Boolean isSaved() {
		return null != ClientsManager.Instance.search(address64);
	}

	public Boolean save() {
		return null != ClientsManager.Instance.save(this);
	}

	public Boolean load() {

		Client savedClient = ClientsManager.Instance.search(address64);

		if( null == savedClient ) {
			Log.debug(LogLevel.VERBOSE,"Client","load", "Impossible to load the client with address: " + address64);
			return false;
		}

		name = savedClient.name;
		mqttClient = savedClient.mqttClient;
		connection = savedClient.connection;
		state = savedClient.state;
		duration = savedClient.duration;
		willTopicReq = savedClient.willTopicReq;
		willMessageReq = savedClient.willMessageReq;

		messages.clear();
		messages.addAll(savedClient.messages);

		return true;
	}

	public String toString() {
		if( "" == name ) {
			return address64.toString();
		} else {
			return name;
		}
	}
}