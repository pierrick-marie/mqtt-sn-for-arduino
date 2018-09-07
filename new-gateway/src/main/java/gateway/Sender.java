package gateway;

import gateway.serial.SerialPortWriter;
import utils.Client;
import utils.Time;
import utils.Utils;
import utils.Log;

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

		byte[] serialMessage = new byte[7 + message.getBody().length()];
		byte[] data = message.getBody().getBytes();
		int nbTry = 0;
		int i;

		// creating the serial message to send

		serialMessage[0] = (byte) serialMessage.length;
		serialMessage[1] = (byte) 0x0C;
		serialMessage[2] = (byte) 0x00;

		Log.debug("Sender", "sendMessage", "topic = " + message.topic());

		serialMessage[3] = Utils.getTopicId(message.topic())[0];
		serialMessage[4] = Utils.getTopicId(message.topic())[1];

		if (Main.MessageId > 255) {
			serialMessage[5] = (byte) (Main.MessageId / 256);
			serialMessage[6] = (byte) (Main.MessageId % 256);
		} else {
			serialMessage[5] = (byte) 0x00;
			serialMessage[6] = (byte) Main.MessageId;
		}

		for (i = 0; i < message.body.length(); i++) {
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
			Log.debug("Sender", "sendMessage", "Resend the message");
			sendMessage();
		}
	}

	public void run() {
		sendMessage();
	}
}
