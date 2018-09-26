package gateway;

import gateway.serial.SerialPortWriter;
import mqtt.Topics;
import utils.*;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Sender extends Thread {

	private final Client client;
	private final Message message;

	public Sender(final Client client, final Message message) {

		this.client = client;
		this.message = message;
	}

	private void sendMessage() {

		byte[] serialMessage = new byte[7 + message.body().length()];
		byte[] data = message.body().getBytes();
		int nbTry = 0;
		int i;

		// creating the serial message to send

		serialMessage[0] = (byte) serialMessage.length;
		serialMessage[1] = (byte) 0x0C;
		serialMessage[2] = (byte) 0x00;

		Log.debug(LogLevel.ACTIVE,"Sender", "sendMessage", "topic = " + message.topic());

		serialMessage[3] = getTopicId(message.topic())[0];
		serialMessage[4] = getTopicId(message.topic())[1];

		if (Main.MessageId > 255) {
			serialMessage[5] = (byte) (Main.MessageId / 256);
			serialMessage[6] = (byte) (Main.MessageId % 256);
		} else {
			serialMessage[5] = (byte) 0x00;
			serialMessage[6] = (byte) Main.MessageId;
		}

		for (i = 0; i < message.body().length(); i++) {
			serialMessage[7 + i] = data[i];
		}

		SerialPortWriter.write(client, serialMessage);

		// waiting for an acquittal
		while (nbTry < 10 && !Main.MessageIdAck.contains(Main.MessageId)) {
			Time.sleep((long) 1000, "Sender.sendMessage()");
			nbTry++;

		}

		// the message has not been acquit -> resend
		if (!Main.MessageIdAck.contains(Main.MessageId)) {
			Log.debug(LogLevel.ACTIVE,"Sender", "sendMessage", "Resend the message");
			sendMessage();
		}
	}

	private byte[] getTopicId(final String name) {

		byte[] ret = new byte[2];
		int id = Topics.list.get(name);

		if (id != -1) {
			if (id > 255) {
				ret[0] = (byte) (id / 255);
				ret[1] = (byte) (id % 255);
			} else {
				ret[0] = (byte) 0x00;
				ret[1] = (byte) id;
			}
		} else {
			return null;
		}

		return ret;
	}

	public void run() {
		sendMessage();
	}
}
