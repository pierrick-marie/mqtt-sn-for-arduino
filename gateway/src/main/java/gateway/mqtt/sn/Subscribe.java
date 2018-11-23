package gateway.mqtt.sn;

import gateway.serial.SerialPortWriter;
import gateway.mqtt.SnTopic;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Subscribe implements SnAction {

	final Client client;
	final byte[] msg;

	public Subscribe(final Client client, final byte[] msg) {

		Log.input(client, "subscribe");

		this.client = client;
		this.msg = msg;
	}

	public void subscribe() {

		byte flags = msg[0];
		byte[] messageId = new byte[2];
		messageId[0] = msg[1];
		messageId[1] = msg[2];

		if (null == client.mqttClient() || !client.mqttClient().isConnected()) {
			Log.error("Subscribre", "subscribe", client + "is not connected");
			suback(new byte[]{(byte)Prtcl.DEFAUlT_QOS.ordinal()}, messageId, 0, Prtcl.REJECTED);
			return;
		}

		byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++) {
			name[i] = msg[3 + i];
		}
		String topicName = new String(name, StandardCharsets.UTF_8);

		synchronized (client.Topics) {

			SnTopic topic = client.Topics.get(topicName);

			if (null != topic) {
				if (!topic.isSubscribed()) {
					try {
						client.mqttClient().subscribe(client, topic);
						Log.debug(LogLevel.ACTIVE, "Subscribe", "subscribe", "subcription ok -> sending sub ack message");
					} catch (TimeoutException e) {
						Log.error("Subscribre", "subscribe", "imposible to subscribe to the topic: " + topicName);
						Log.debug(LogLevel.VERBOSE, "Subscribre", "subscribe", e.getMessage());
						suback(new byte[]{(byte) Prtcl.DEFAUlT_QOS.ordinal()}, messageId, topic.id(), Prtcl.REJECTED);
						return;
					}
				}
				Log.debug(LogLevel.ACTIVE, "Subscribe", "subscribe", "Topics " + topicName + " is already registered with id: " + topic.id());
				suback(new byte[]{(byte) Prtcl.DEFAUlT_QOS.ordinal()}, messageId, topic.id(), Prtcl.ACCEPTED);
			} else {
				Log.error("Subscribe", "subscribe", "Topics NOT registered " + client.Topics.get(1));
				// Error - topicId = -1
				suback(new byte[]{(byte) Prtcl.DEFAUlT_QOS.ordinal()}, messageId, -1, Prtcl.REJECTED);
			}
		}
	}

	private void suback(final byte[] qos, final byte[] messageId, final int topicId, final byte returnCode) {

		Log.output(client, "sub ack");

		byte[] ret = new byte[8];
		ret[0] = (byte) 0x08;
		ret[1] = (byte) 0x13;
		ret[2] = qos[0];
		if (topicId > 255) {
			ret[3] = (byte) (topicId / 255);
			ret[4] = (byte) (topicId % 255);
		} else {
			ret[3] = (byte) 0x00;
			ret[4] = (byte) topicId;
		}
		ret[5] = messageId[0];
		ret[6] = messageId[1];
		ret[7] = returnCode;

		SerialPortWriter.write(client, ret);
	}

	@Override
	public void exec() {
		subscribe();
	}
}
