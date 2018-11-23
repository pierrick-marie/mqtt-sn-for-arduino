package gateway.mqtt.sn;

import gateway.serial.SerialPortWriter;
import gateway.mqtt.SnTopic;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Register implements SnAction {

	private final Client client;
	private final byte[] message;

	public Register(final Client client, final byte[] msg) {

		Log.input(client, "register");

		this.client = client;
		this.message = msg;
	}

	/**
	 * This method registers a topic name into the list of Topics @see:Main.TopicName
	 * The method does not register the topic name to the bus.
	 */
	@Override
	public void exec() {

		byte[] messageId = new byte[2];
		messageId[0] = message[2];
		messageId[1] = message[3];
		byte[] name = new byte[message.length - 4];
		String topicName;
		int i;
		SnTopic topic;

		if (null == client.mqttClient() || !client.mqttClient().isConnected()) {
			Log.error("Register", "register", client + "is not connected");
			// Error - topicId = -1
			regack(-1, messageId, Prtcl.REJECTED);
			return;
		}

		for (i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		topicName = new String(name, StandardCharsets.UTF_8);

		synchronized (client.Topics) {

			topic = client.Topics.get(topicName);

			if (null != topic) {
				Log.debug(LogLevel.ACTIVE, "Register", "register", "topic " + topic.name() + " (id:" + topic.id() + ") is already registered");
			} else {
				Log.debug(LogLevel.ACTIVE, "Register", "register", "topic " + topicName + " is NOT contained -> saving the topic with id: " + client.Topics.size());
				topic = client.Topics.put(client.Topics.size(), topicName);
			}
			regack(topic.id(), messageId, Prtcl.ACCEPTED);
		}
	}

	/**
	 * The method sends regack message to the XBee module.
	 *
	 * @param messageId
	 * @param topicId
	 */
	private void regack(final int topicId, final byte[] messageId, final byte returnCode) {

		Log.output(client, "reg ack");

		// the message to send
		byte[] message = new byte[7];

		message[0] = (byte) 0x07;
		message[1] = (byte) 0x0B;
		if (topicId > 255) {
			message[2] = (byte) (topicId % 255);
			message[3] = (byte) (topicId / 255);
		} else {
			message[2] = (byte) topicId;
			message[3] = (byte) 0x00;
		}
		message[4] = messageId[0];
		message[5] = messageId[1];
		message[6] = returnCode;

		SerialPortWriter.write(client, message);
	}
}
