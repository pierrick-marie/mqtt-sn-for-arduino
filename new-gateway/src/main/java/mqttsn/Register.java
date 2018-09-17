package mqttsn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import utils.Client;
import utils.Log;
import utils.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Register extends Thread {

	private final Client client;
	private final byte[] message;

	public Register(final Client client, final byte[] msg) {
		this.client = client;
		this.message = msg;
	}

	/**
	 * This method registers a topic name into the list of topics @see:Main.TopicName
	 * The method does not register the topic name to the bus.
	 */
	private void register() {

		Log.input(client, "register");

		byte[] messageId = new byte[2];
		messageId[0] = message[2];
		messageId[1] = message[3];
		byte[] name = new byte[message.length - 4];
		String topicName;
		int i, topicId = -1;

		for (i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		topicName = new String(name, StandardCharsets.UTF_8);

		if (Main.TopicName.containsKey(topicName)) {
			topicId = Main.TopicName.get(topicName);

			Log.debug(LogLevel.ACTIVATED,"Register", "register","topic " + topicName + " (id:" + topicId + ") is contained");
		} else {
			topicId = Main.TopicName.size();
			Log.debug(LogLevel.ACTIVATED,"Register", "register","topic " + topicName + " (id:" + topicId + ") is NOT contained");
			Main.TopicName.put(topicName, topicId);
		}
		if (topicId != -1)
			regack(messageId, topicId);
		else {
			Log.error("Register", "register","TopicName:" + topicName);
		}
	}

	/**
	 * The method sends regack message to the XBee module.
	 *
	 * @param messageId
	 * @param topicId
	 */
	private void regack(final byte[] messageId, final int topicId) {

		Log.output(client, "regack");

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
		message[6] = (byte) 0x00;

		SerialPortWriter.write(client, message);
	}

	public void run() {
		register();
	}


}
