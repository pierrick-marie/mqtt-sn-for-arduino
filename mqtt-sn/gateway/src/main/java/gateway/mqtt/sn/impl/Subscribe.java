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
import gateway.serial.SerialPortWriter;
import gateway.utils.log.Log;

public class Subscribe implements Runnable {

	final Device device;
	final byte[] msg;

	public Subscribe(final Device device, final byte[] msg) {

		Log.input(device, "subscribe");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void run() {
		subscribe();
	}

	private void suback(final byte[] messageId, final int topicId, final byte returnCode) {

		Log.output(device, "sub ack with return code: " + returnCode);

		final byte[] message = new byte[8];
		message[0] = (byte) 0x08;
		message[1] = (byte) 0x13;
		message[2] = Prtcl.DEFAULT_QOS;

		if (topicId > 255) {
			message[2] = (byte) (topicId % 255);
			message[3] = (byte) (topicId / 255);
		} else {
			message[3] = (byte) topicId;
			message[2] = (byte) 0x00;
		}

		message[5] = messageId[0];
		message[6] = messageId[1];
		message[7] = returnCode;

		SerialPortWriter.write(device, message);
	}

	public void subscribe() {

		// NOT IMPLEMENTED YET (QoS)
		// final byte flags = msg[0];
		final byte[] messageId = new byte[2];
		messageId[0] = msg[1];
		messageId[1] = msg[2];

		if (!device.isConnected()) {
			Log.error("Subscribre", "subscribe", device + "is not connected");
			suback(messageId, -1, Prtcl.REJECTED);
			return;
		}

		final byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++) {
			name[i] = msg[3 + i];
		}
		final String topicName = new String(name, StandardCharsets.UTF_8);

		final Topic topic = device.subscribe(topicName);
		if (null != topic) {
			suback(messageId, 298, Prtcl.ACCEPTED);
		} else {
			suback(messageId, -1, Prtcl.REJECTED);
		}
	}
}
