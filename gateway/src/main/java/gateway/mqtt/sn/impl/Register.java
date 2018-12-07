/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import java.nio.charset.StandardCharsets;

import gateway.mqtt.client.Device;
import gateway.mqtt.impl.Topic;
import gateway.mqtt.sn.IAction;
import gateway.serial.SerialPortWriter;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Register implements IAction {

	private final Device device;
	private final byte[] message;

	public Register(final Device device, final byte[] msg) {

		Log.input(device, "register");

		this.device = device;
		message = msg;
	}

	/**
	 * This method registers a topic name into the list of
	 * Topics @see:Main.TopicName The method does not register the topic name to the
	 * bus.
	 */
	@Override
	public void exec() {

		final byte[] messageId = new byte[2];
		messageId[0] = message[2];
		messageId[1] = message[3];
		final byte[] name = new byte[message.length - 4];
		String topicName;
		int i;
		Topic topic;

		if (!device.isConnected()) {
			Log.error("Register", "register", device + "is not connected");
			// Error - topicId = -1
			regack(-1, messageId, Prtcl.REJECTED);
			return;
		}

		for (i = 0; i < name.length; i++) {
			name[i] = message[4 + i];
		}

		topicName = new String(name, StandardCharsets.UTF_8);

		topic = device.getTopic(topicName);

		if (null != topic) {
			Log.debug(LogLevel.ACTIVE, "Register", "register",
					"topic " + topic.name() + " (id:" + topic.id() + ") is already registered");
		} else {
			Log.debug(LogLevel.ACTIVE, "Register", "register", "topic " + topicName
					+ " is NOT contained -> saving the topic with id: " + device.nbTopics());
			topic = device.addTopic(device.nbTopics(), topicName);
		}
		regack(topic.id(), messageId, Prtcl.ACCEPTED);
	}

	/**
	 * The method sends regack message to the XBee module.
	 *
	 * @param messageId
	 * @param topicId
	 */
	private void regack(final int topicId, final byte[] messageId, final byte returnCode) {

		Log.output(device, "reg ack");

		// the message to send
		final byte[] message = new byte[7];

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

		SerialPortWriter.write(device, message);
	}
}
