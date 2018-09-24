package mqttsn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import org.fusesource.mqtt.client.Callback;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;
import utils.Utils;

import java.util.*;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Publish extends Thread {

	private final Client client;
	private final byte[] msg;

	public Publish(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	final void publish() {

		Log.output(client, "Publish");

		byte flags = msg[0];
		boolean DUP = (flags & 0b10000000) == 1;
		int qos = flags & 0b01100000 >> 5;
		boolean retain = (flags & 0b00010000) == 1;
		int topicIDType = flags & 0b00000011;
		int topicID = (msg[2] << 8) + (msg[1] & 0xFF);

		byte[] msgID = new byte[2];
		msgID[0] = msg[3];
		msgID[1] = msg[4];

		byte[] data = new byte[msg.length - 5];
		for (int i = 0; i < data.length; i++) {
			data[i] = msg[5 + i];
		}

		if (Main.TopicName.containsValue(topicID)) {

			String topicName = getKeyByValue(Main.TopicName, topicID);

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
				puback(topicID, msgID, 0x03);
			}
		} else {
			reregister(topicID, msgID);
		}
	}

	private void reregister(final int topicId, final byte[] msgID) {

		Log.input(client, "Re Register");

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
		ret[4] = msgID[0];
		ret[5] = msgID[1];
		ret[6] = (byte) 0x00;

		SerialPortWriter.write(client, ret);
	}

	private void puback(final int topicId, final byte[] msgID, final int returnCode) {

		Log.input(client, "Puback");

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
		ret[4] = msgID[0];
		ret[5] = msgID[1];
		ret[6] = (byte) returnCode;

		SerialPortWriter.write(client, ret);
	}

	public void run() {
		publish();
	}

	private String getKeyByValue(Map<String, Integer> map, int value) {

		String key = "";

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				key = entry.getKey();
			}
		}

		return key;
	}


}
