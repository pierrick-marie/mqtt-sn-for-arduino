package gateway;

import gateway.serial.SerialPortReader;
import utils.log.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

	public static int GatewayId = 1;

	// @todo DEBUG: use the ClientManager instead
	public final static HashMap<String, Integer> TopicName = new HashMap<>();
	public final static ArrayList<Integer> MessageIdAck = new ArrayList<>();

	public static final String SERIAL_PORT = "/dev/ttyUSB0";
	public static final String HOST = "141.115.64.26";
	public static final Integer PORT = 1883;

	public static int MessageId = 0;

	public static void main(String[] args) {

		Log.print("Starting the gateway, waiting for connections...");

		new SerialPortReader();
	}
}

