package mqtt.sn;

import gateway.serial.SerialPortWriter;
import mqtt.Topics;
import utils.client.Client;
import utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Publish extends Thread {

	private final Client client;
	private final byte[] msg;

	public Publish(final Client client, final byte[] msg) {

		Log.output(client, "publish");

		this.client = client;
		this.msg = msg;
	}

	final void publish() {

		byte flags = msg[0];
		boolean DUP = (flags & 0b10000000) == 1;
		int qos = flags & 0b01100000 >> 5;
		boolean retain = (flags & 0b00010000) == 1;
		int topicIDType = flags & 0b00000011;
		int topicId = (msg[2] << 8) + (msg[1] & 0xFF);

		byte[] messageId = new byte[2];
		messageId[0] = msg[3];
		messageId[1] = msg[4];

		if (null == client.mqttClient() || !client.mqttClient().isConnected()) {
			Log.error("Publish", "publish", client + "is not connected");
			puback(topicId, messageId, Prtcl.REJECTED);
			return;
		}

		byte[] data = new byte[msg.length - 5];
		for (int i = 0; i < data.length; i++) {
			data[i] = msg[5 + i];
		}

		if (Topics.list.contains(topicId)) {

			String topicName = Topics.list.get(topicId);

			// @TODO: DEBUG
			// if (null != client.connection()) {
			if (true) {
				/**
				 *
				 * @TODO: DEBUG
				 *
				client.connection().publish(topicName, data, Utils.getQoS(qos), retain, new Callback<Void>() {
					@Override
					public void onSuccess(Void value) {
						puback(topicID, msgID, 0x00);
					}

					@Override
					public void onFailure(Throwable e) {
						Log.error("Publish", "publish", "Error on publish");
						Log.debug(LogLevel.ACTIVE,"Publish", "publish", e.getMessage());
					}
				});
				 **/
			} else {
				puback(topicId, messageId, 0x03);
			}
		} else {
			reregister(topicId, messageId);
		}
	}

	private void reregister(final int topicId, final byte[] messageId) {

		Log.output(client, "re register");

		byte[] ret = new byte[7];
		ret[0] = (byte) 0x07;
		ret[1] = (byte) 0x1E;
		if (topicId > 255) {
			ret[2] = (byte) (topicId / 255);
			ret[3] = (byte) (topicId % 255);
		} else {
			ret[2] = (byte) 0x00;
			ret[3] = (byte) topicId;
		}
		ret[4] = messageId[0];
		ret[5] = messageId[1];
		ret[6] = Prtcl.ACCEPTED;

		SerialPortWriter.write(client, ret);
	}

	private void puback(final int topicId, final byte[] messageId, final int returnCode) {

		Log.input(client, "pub ack");

		byte[] ret = new byte[7];
		ret[0] = (byte) 0x07;
		ret[1] = (byte) 0x0D;
		if (topicId > 255) {
			ret[2] = (byte) (topicId / 255);
			ret[3] = (byte) (topicId % 255);
		} else {
			ret[2] = (byte) 0x00;
			ret[3] = (byte) topicId;
		}
		ret[4] = messageId[0];
		ret[5] = messageId[1];
		ret[6] = (byte) returnCode;

		SerialPortWriter.write(client, ret);
	}

	public void run() {
		publish();
	}
}
