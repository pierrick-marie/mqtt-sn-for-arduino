package utils;


// public class ClientsState {

import gateway.Message;
import org.fusesource.mqtt.client.MQTT;

import java.util.ArrayList;
import java.util.HashMap;

enum State {
	ASLEEP("Asleep"),	LOST("Lost"), ACTIVE("Active");

	private String state;

	public String getState() {
		return this.state;
	}

	State(final String state) {
		this.state = state;
	}
}

public class Client {

	private String name = null;
	private String id = null;
	private MQTT mqttClient = null;
	private String address = null;
	private State state = null;
	private Integer duration = null;

	public final ArrayList<Message> messages = new ArrayList<>();

	public Client() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MQTT getMqttClient() {
		return mqttClient;
	}

	public void setMqttClient(MQTT mqttClient) {
		this.mqttClient = mqttClient;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}