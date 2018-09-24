package mqttsn;

import utils.client.Client;
import utils.log.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessage extends Thread {

	private Client client;
	private byte[] msg;

	public WillMessage(final Client client, final byte[] msg) {

		Log.input(client, "Will message");

		this.client = client;
		this.msg = msg;
	}

	public void willmessage() {

		client.setWillMessageAck(false);

		String willmessage = new String(msg, StandardCharsets.UTF_8);

		client.mqttClient().setWillMessage(willmessage);

	}

	public void run() {
		willmessage();
	}
}
