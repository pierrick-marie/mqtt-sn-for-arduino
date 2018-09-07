package utils;

import gateway.Message;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

import java.util.ArrayList;

public class Client {

	private String name = "";
	private Integer duration = 0;
	private State state = State.DISCONNECTED;
	private MQTT mqttClient = null;
	private byte[] address64 = null;
	private byte[] address16 = null;

	private CallbackConnection connection = null;
	public final ArrayList<Message> messages = new ArrayList<>();

	protected Client() { }

	public byte[] address16() {
		return address16;
	}

	public void setAddress16(final byte[] address) {
		if( null == address) { Log.error("Client", "setAddress16", "asddress is null"); }
		address16 = address;
	}

	public CallbackConnection connection() {
		return connection;
	}

	public void setConnection(final CallbackConnection connection) {
		if( null == connection) { Log.error("Client", "setConnection", "connection is null"); }
		this.connection = connection;
	}

	public String name() {
		return name;
	}

	public void setName(final String name) {
		if( null == name) { Log.error("Client", "setName", "name is null"); }
		this.name = name;
	}

	public MQTT mqttClient() {
		return mqttClient;
	}

	public void setMqttClient(final MQTT mqttClient) {
		if( null == mqttClient) { Log.error("Client", "setMqttClient", "mqttClient is null"); }
		this.mqttClient = mqttClient;
	}

	public byte[] address64() {
		return address64;
	}

	public void setAddress64(final byte[] address) {
		if( null == address) { Log.error("Client", "setAddress64", "address is null"); }
		address64 = address;
	}

	public State state() {
		return state;
	}

	public void setState(final State state) {
		if( null == state) { Log.error("Client", "setState", "state is null"); }
		this.state = state;
	}

	public Integer duration() {
		return duration;
	}

	public void setDuration(final Integer duration) {
		if( null == duration) { Log.error("Client", "setDuration", "duration is null"); }
		this.duration = duration;
	}

	public String toString() {
		return name;
	}

	public String getStringAddress64() {

		return addressToString(address64);
	}

	public String getStringAddress16() {

		return addressToString(address16);
	}

	public static String addressToString(final byte[] address) {

		String ret = "";

		for (int i = 0; i < address.length; i++) {
			ret += address[i];
		}

		return ret;
	}
}