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

public class Subscribe implements IAction {

	final Device device;
	final byte[] msg;

	public Subscribe(final Device device, final byte[] msg) {

		Log.input(device, "subscribe");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void exec() {
		subscribe();
	}

	private void suback(final byte[] qos, final byte[] messageId, final int topicId, final byte returnCode) {

		Log.output(device, "sub ack with return code: " + returnCode);

		final byte[] ret = new byte[8];
		ret[0] = (byte) 0x08;
		ret[1] = (byte) 0x13;
		ret[2] = qos[0];
		if (topicId > 255) {
			ret[3] = (byte) (topicId / 255);
			ret[4] = (byte) (topicId % 255);
		} else {
			ret[3] = (byte) 0x00;
			ret[4] = (byte) topicId;
		}
		ret[5] = messageId[0];
		ret[6] = messageId[1];
		ret[7] = returnCode;

		SerialPortWriter.write(device, ret);
	}

	public void subscribe() {

		// NOT IMPLEMENTED YET (QoS)
		// final byte flags = msg[0];
		final byte[] messageId = new byte[2];
		messageId[0] = msg[1];
		messageId[1] = msg[2];

		if (!device.isConnected()) {
			Log.error("Subscribre", "subscribe", device + "is not connected");
			suback(new byte[] { (byte) Prtcl.DEFAULT_QOS }, messageId, 0, Prtcl.REJECTED);
			return;
		}

		final byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++) {
			name[i] = msg[3 + i];
		}
		final String topicName = new String(name, StandardCharsets.UTF_8);

		final Topic topic = device.subscribe(topicName);
		if (null != topic) {
			suback(new byte[] { (byte) Prtcl.DEFAULT_QOS }, messageId, topic.id(), Prtcl.ACCEPTED);
		} else {
			suback(new byte[] { (byte) Prtcl.DEFAULT_QOS }, messageId, -1, Prtcl.REJECTED);
		}
	}
}
