package mqtt.sn;

import org.fusesource.mqtt.client.QoS;
import utils.client.Client;
import utils.log.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillTopic {

	private Client client;
	private byte[] msg;

	public WillTopic(final Client client, final byte[] msg) {

		Log.input(client, "Will topic");

		this.client = client;
		this.msg = msg;

		willtopic();
	}

	public void willtopic() {

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
			client.mqttClient().setWillQos(Prtcl.getQoS(will_QOS));
			client.mqttClient().setWillRetain(will_retain);
		}
	}
}
