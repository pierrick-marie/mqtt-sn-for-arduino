package gateway;

import gateway.serial.SerialPortReader;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import utils.State;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

	public static final Boolean DEBUG = true;
	public static final Boolean ERROR = true;

	public static int GatewayId = 1;

	// @todo DEBUG: use the ClientManager instead
	public final static HashMap<String, MQTT> ClientMap = new HashMap<>();
	public final static HashMap<String, String> AddressClientMap = new HashMap<>();
	public final static HashMap<String, State> ClientState = new HashMap<>();
	public final static HashMap<String, Integer> ClientDuration = new HashMap<>();
	public final static HashMap<String, ArrayList<Message>> ClientBufferedMessage = new HashMap<>();
	public final static HashMap<String, CallbackConnection> AddressConnectionMap = new HashMap<>();
	public final static HashMap<String, Integer> TopicName = new HashMap<>();
	public final static ArrayList<Integer> MessageIdAck = new ArrayList<>();
	public final static HashMap<String, Boolean> WillTopicAck = new HashMap<>();
	public final static HashMap<String, Boolean> WillMessageAck = new HashMap<>();

	public static final String SERIAL_PORT = "/dev/ttyUSB0";
	public static final String HOST = "141.115.64.26";
	public static final Integer PORT = 1883;

	public static int MessageId = 0;

	public static void main(String[] args) {

		new SerialPortReader();
	}
}

