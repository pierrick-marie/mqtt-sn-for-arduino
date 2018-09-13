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
public class WillTopic extends Thread {

	private Client client;
	private byte[] msg;

	public WillTopic(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	public void willtopic() {

		Log.output(client, "Will topic");

		client.setWillTopicAck(false);

		if (msg.length == 0) {

			client.mqttClient().setWillMessage("");
			client.mqttClient().setWillTopic("");

		} else {

			byte flags = msg[0];
			int will_QOS = flags & 0b01100000 >> 5;
			boolean will_retain = (flags & 0b00010000) == 1;
			byte[] data = new byte[msg.length - 1];

			for (int i = 0; i < msg.length - 1; i++) {
				data[i] = msg[i + 1];
			}

			String willtopic = new String(data, StandardCharsets.UTF_8);

			client.mqttClient().setWillTopic(willtopic);
			client.mqttClient().setWillQos(Utils.getQoS(will_QOS));
			client.mqttClient().setWillRetain(will_retain);
		}
	}

	public void run() {
		willtopic();
	}
}
