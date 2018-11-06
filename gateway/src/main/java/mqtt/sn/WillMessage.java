package mqtt.sn;

import utils.client.Client;
import utils.log.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessage {

	private Client client;
	private byte[] msg;

	public WillMessage(final Client client, final byte[] msg) {

		Log.input(client, "Will message");

		this.client = client;
		this.msg = msg;

		willmessage();
	}

	public void willmessage() {

		client.setWillMessageAck(false);

		String willmessage = new String(msg, StandardCharsets.UTF_8);

		client.mqttClient().setWillMessage(willmessage);
	}
}
