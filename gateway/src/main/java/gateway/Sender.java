package gateway;

import gateway.serial.SerialPortWriter;
import mqtt.Topics;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Sender {

	private final Client client;

	private static volatile int messageId = 0;

	public Sender(final Client client) {

		this.client = client;
	}

	public void send(final MqttMessage mqttMessage) {

		byte[] serialMessage = new byte[7 + mqttMessage.body().length()];
		byte[] data = mqttMessage.body().getBytes();
		int i;

		// creating the serial mqttMessage to send

		serialMessage[0] = (byte) serialMessage.length;
		serialMessage[1] = (byte) 0x0C;
		serialMessage[2] = (byte) 0x00;

		Log.debug(LogLevel.ACTIVE,"Sender", "sendMessage", "topic = " + mqttMessage.topic());

		serialMessage[3] = getTopicId(mqttMessage.topic())[0];
		serialMessage[4] = getTopicId(mqttMessage.topic())[1];

		if (messageId > 255) {
			serialMessage[5] = (byte) (messageId / 256);
			serialMessage[6] = (byte) (messageId % 256);
		} else {
			serialMessage[5] = (byte) 0x00;
			serialMessage[6] = (byte) messageId;
		}

		for (i = 0; i < mqttMessage.body().length(); i++) {
			serialMessage[7 + i] = data[i];
		}

		SerialPortWriter.write(client, serialMessage);
		mqttMessage.setMessageId(messageId);
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
}
