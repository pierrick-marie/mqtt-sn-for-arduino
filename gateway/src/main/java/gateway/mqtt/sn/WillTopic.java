package gateway.mqtt.sn;

import gateway.mqtt.client.Client;
import gateway.utils.log.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillTopic implements SnAction {

	private Client client;
	private byte[] msg;

	public WillTopic(final Client client, final byte[] msg) {

		Log.input(client, "Will topic");

		this.client = client;
		this.msg = msg;
	}

	public void willtopic() {

		if (msg.length == 0) {

			client.mqttClient().setWillMessage("");
			client.mqttClient().setWillTopic("");

		} else {

			byte flags = msg[0];
			boolean will_retain = (flags & 0b00010000) == 1;
			byte[] data = new byte[msg.length - 1];

			for (int i = 0; i < msg.length - 1; i++) {
				data[i] = msg[i + 1];
			}

			String willtopic = new String(data, StandardCharsets.UTF_8);

			client.mqttClient().setWillTopic(willtopic);
			client.mqttClient().setWillQos(Prtcl.DEFAUlT_QOS);
			client.mqttClient().setWillRetain(will_retain);
		}
	}

	@Override
	public void exec() {
		willtopic();
	}
}
