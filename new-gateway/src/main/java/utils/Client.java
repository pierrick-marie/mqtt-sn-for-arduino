package utils;

import gateway.Message;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.address.Address16;
import utils.address.Address64;

import java.util.ArrayList;

public class Client {

	private String name = "unknown";
	private Integer duration = 0;
	private State state = State.DISCONNECTED;
	private MQTT mqttClient = null;
	private Boolean willTopicReq = false;
	private Boolean willMessageReq = false;
	private CallbackConnection connection = null;

	public final Address64 address64;
	public final Address16 address16;

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
			Log.error("Client","load", "Impossible to find the saved client");
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
		return name;
	}
}