package utils.client;

import gateway.Message;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.State;
import utils.address.Address16;
import utils.address.Address64;
import utils.log.Log;
import utils.log.LogLevel;
import utils.mqttclient.MqttClient;

import java.util.ArrayList;

public class Client {

	private String name = "";
	private Integer duration = 0;
	private State state = State.DISCONNECTED;
	private MqttClient mqttClient = null;
	private Boolean willTopicReq = false;
	private Boolean willTopicAck = false;
	private Boolean willMessageAck = false;
	private Boolean willMessageReq = false;
	// private CallbackConnection connection = null;

	public Address64 address64 = null;
	public Address16 address16 = null;

	public final ArrayList<Message> messages = new ArrayList<>();

	public Client(final Address64 address64, final Address16 address16) {
		this.address64 = address64;
		this.address16 = address16;
		state = State.FIRSTCONNECT;
	}

	/*
	public CallbackConnection connection() {
		return connection;
	}
	*/

	/*
	public Client setConnection(final CallbackConnection connection) {
		if (null == connection) {
			Log.error("Client", "setConnection", "connection is null");
		}

		this.connection = connection;

		Log.debug(LogLevel.VERBOSE,"Client", "setConnect", "Register client's connection with " + connection);
		save();

		return this;
	}
	*/

	public String name() {
		return name;
	}

	public Client setName(final String name) {

		if (null == name) {
			Log.error("Client", "setName", "name is null");
		}

		this.name = name;

		Log.debug(LogLevel.VERBOSE,"Client", "setName", "Register client's name with " + name);
		save();

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

		Log.debug(LogLevel.VERBOSE,"Client", "setState", "Register client's state with " + state);
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

		Log.debug(LogLevel.VERBOSE,"Client", "setDuration", "Register client's duration with " + duration);
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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillTopicReq", "Register client's willTopicReq with " + willTopicReq);
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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillTopicAck", "Register client's willTopicAck with " + willTopicAck);
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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillMessageAck", "Register client's willMessageAck with " + willMessageAck);
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

		Log.debug(LogLevel.VERBOSE,"Client", "setWillMessageReq", "Register client's willMessageReq with " + willMessageReq);
		save();

		return this;
	}

	public Boolean isSaved() {
		return null != ClientsManager.Instance.search(address64);
	}

	public Boolean save() {
		Log.debug(LogLevel.ACTIVE,"Client","save", "saving the client " + this);
		return null != ClientsManager.Instance.save(this);
	}

	public Boolean load() {

		Log.debug(LogLevel.ACTIVE,"Client","load", "searching the client with address " + address64);
		Client savedClient = ClientsManager.Instance.search(address64);

		if( null == savedClient ) {
			Log.debug(LogLevel.ACTIVE,"Client","load", "client with address " + address64 + " is unknown");
			return false;
		}

		name = savedClient.name;
		mqttClient = savedClient.mqttClient;
		// connection = savedClient.connection;
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