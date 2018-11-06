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

	private final Integer NB_TRY_ACQUITTAL = 40;
	private final long TIME_TO_WAIT = 250; // milliseconds

	private final Client client;
	private final MqttMessage mqttMessage;

	private static volatile boolean ReceivedAcquittal = false;
	private static volatile int AcquittalId = 0;
	private static volatile int MessageId = 0;

	public Sender(final Client client, final MqttMessage mqttMessage) {

		this.client = client;
		this.mqttMessage = mqttMessage;
	}

	private void sendMessage() {

		byte[] serialMessage = new byte[7 + mqttMessage.body().length()];
		byte[] data = mqttMessage.body().getBytes();
		int nbTry = 0;
		int i;

		// creating the serial mqttMessage to send

		serialMessage[0] = (byte) serialMessage.length;
		serialMessage[1] = (byte) 0x0C;
		serialMessage[2] = (byte) 0x00;

		Log.debug(LogLevel.ACTIVE,"Sender", "sendMessage", "topic = " + mqttMessage.topic());

		serialMessage[3] = getTopicId(mqttMessage.topic())[0];
		serialMessage[4] = getTopicId(mqttMessage.topic())[1];

		if (MessageId > 255) {
			serialMessage[5] = (byte) (MessageId / 256);
			serialMessage[6] = (byte) (MessageId % 256);
		} else {
			serialMessage[5] = (byte) 0x00;
			serialMessage[6] = (byte) MessageId;
		}

		for (i = 0; i < mqttMessage.body().length(); i++) {
			serialMessage[7 + i] = data[i];
		}

		SerialPortWriter.write(client, serialMessage);

		while(!ReceivedAcquittal && nbTry < NB_TRY_ACQUITTAL) {
			Time.sleep(TIME_TO_WAIT, "Sender.sendMessage(): waiting for acquittal");
		}

		if(AcquittalId == MessageId) {
			MessageId++;
		} else {
			Log.debug(LogLevel.ACTIVE,"Sender", "sendMessage", "Resend the mqttMessage");
			sendMessage();
		}
	}

	public static void acquittal(final Integer messageId){
		ReceivedAcquittal = true;
		AcquittalId = messageId;
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
