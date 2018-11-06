package mqtt.sn;

import gateway.serial.SerialPortWriter;
import mqtt.Topics;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Register {

	private final Client client;
	private final byte[] message;

	public Register(final Client client, final byte[] msg) {

		Log.input(client, "register");

		this.client = client;
		this.message = msg;

		register();
	}

	/**
	 * This method registers a topic name into the list of topics @see:Main.TopicName
	 * The method does not register the topic name to the bus.
	 */
	private void register() {

		byte[] messageId = new byte[2];
		messageId[0] = message[2];
		messageId[1] = message[3];
		byte[] name = new byte[message.length - 4];
		String topicName;
		int i, topicId = -1;

		if (null == client.mqttClient() || !client.mqttClient().isConnected()) {
			Log.error("Register", "register", client + "is not connected");
			regack(topicId, messageId, Prtcl.REJECTED);
			return;
		}

		for (i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		topicName = new String(name, StandardCharsets.UTF_8);

		if (Topics.list.contains(topicName)) {
			Log.debug(LogLevel.ACTIVE, "Register", "register", "topic " + topicName + " (id:" + topicId + ") is contained");
			topicId = Topics.list.get(topicName);

		} else {
			topicId = Topics.list.size();
			Topics.list.put(topicName, topicId);
			Log.debug(LogLevel.ACTIVE, "Register", "register", "topic " + topicName + " is NOT contained -> saving the topic with id: " + topicId);
		}
		if (topicId != -1) {
			Log.debug(LogLevel.ACTIVE, "Register", "register", "sending regack message: OK");
			regack(topicId, messageId, Prtcl.ACCEPTED);
		} else {
			Log.debug(LogLevel.ACTIVE, "Register", "register", "topicId = -1 -> sending regack message: KO");
			regack(topicId, messageId, Prtcl.REJECTED);
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
			message[3] = (byte) (topicId / 255);
			message[2] = (byte) (topicId % 255);
		} else {
			message[3] = (byte) topicId;
			message[2] = (byte) 0x00;
		}
		message[4] = messageId[0];
		message[5] = messageId[1];
		message[6] = returnCode;

		SerialPortWriter.write(client, message);
	}

}
