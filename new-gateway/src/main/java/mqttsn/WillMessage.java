package mqttsn;

import gateway.Main;
import org.fusesource.mqtt.client.MQTT;
import utils.Client;
import utils.Log;
import utils.Utils;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessage extends Thread {

	private Client client;
	private byte[] msg;

	public WillMessage(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	public void willmessage() {

		Log.output(client, "Will message");

		client.setWillMessageAck(false);

		String willmessage = new String(msg, StandardCharsets.UTF_8);

		client.mqttClient().setWillMessage(willmessage);

	}

	public void run() {
		willmessage();
	}
}
