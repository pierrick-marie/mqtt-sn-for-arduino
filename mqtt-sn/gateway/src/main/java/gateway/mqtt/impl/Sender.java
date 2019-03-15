/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.impl;

import gateway.mqtt.client.Device;
import gateway.serial.SerialPortWriter;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Sender {

	private static volatile int messageId = 0;

	private final Device device;

	public Sender(final Device device) {

		this.device = device;
	}

	private byte[] getTopicId(final String name) {

		final byte[] ret = new byte[2];
		final int id = device.getTopicId(name);

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

	public void send(final SnMessage message) {

		final byte[] serialMessage = new byte[7 + message.getPayload().length];
		final byte[] data = message.getPayload();
		int i;

		// creating the serial message to send

		serialMessage[0] = (byte) serialMessage.length;
		serialMessage[1] = (byte) 0x0C;
		serialMessage[2] = (byte) 0x00;

		Log.debug(LogLevel.ACTIVE, "Sender", "sendMessage", "topic = " + message.topic());

		serialMessage[3] = getTopicId(message.topic())[0];
		serialMessage[4] = getTopicId(message.topic())[1];

		if (messageId > 255) {
			serialMessage[5] = (byte) (messageId / 256);
			serialMessage[6] = (byte) (messageId % 256);
		} else {
			serialMessage[5] = (byte) 0x00;
			serialMessage[6] = (byte) messageId;
		}

		for (i = 0; i < message.getPayload().length; i++) {
			serialMessage[7 + i] = data[i];
		}

		SerialPortWriter.write(device, serialMessage);
		message.setId(messageId);
	}
}
