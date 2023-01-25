/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.impl.Topic;
import gateway.serial.Writer;
import gateway.utils.log.Log;

public class Publish implements Runnable {

	private final Device device;
	private final byte[] msg;

	public Publish(final Device device, final byte[] msg) {

		Log.xbeeInput(device, "publish");

		this.device = device;
		this.msg = msg;
	}

	// private void reRegister(final int topicId, final byte[] messageId) {
	private void reRegister(final int topicId) {

		Log.xbeeOutput(device, "re register");

		final byte[] ret = new byte[7];
		ret[0] = (byte) 0x07;
		ret[1] = (byte) 0x1E;
		if (topicId > 255) {
			ret[2] = (byte) (topicId % 255);
			ret[3] = (byte) (topicId / 255);
		} else {
			ret[2] = (byte) topicId;
			ret[3] = (byte) 0x00;
		}
		// ret[4] = messageId[0];
		// ret[5] = messageId[1];
		ret[4] = 0x00;
		ret[5] = 0x00;
		ret[6] = Prtcl.ACCEPTED;

		Writer.Instance.write(device, ret);
	}

	@Override
	public void run() {

		// @TODO not implemented yet
		// final byte flags = msg[0];
		// QoS qos = Prtcl.DEFAUlT_QOS;

		final int topicId = (msg[2] << 8) + (msg[1] & 0xFF);

		final byte[] messageId = new byte[2];
		// messageId[0] = msg[3];
		// messageId[1] = msg[4];
		messageId[0] = 0x00;
		messageId[1] = 0x00;

		if (!device.isConnected()) {
			Log.error("Publish", "publish", device + "is not connected");
			// @TODO not used until with QoS level 1 and 2 (not implemented)
			// puback(topicId, messageId, Prtcl.REJECTED);
			return;
		}

		final byte[] data = new byte[msg.length - 5];
		for (int i = 0; i < data.length; i++) {
			data[i] = msg[5 + i];
		}

		if (device.containsTopic(topicId)) {

			final Topic topic = device.getTopic(topicId);

			if (device.publish(topic, new String(data))) {

				Log.info(device + " publish message " + new String(data) + " on topic "
						+ topic.name().toString());
				// TODO not used until with QoS level 1 and 2 (not implemented)
				// puback(topicId, messageId, Prtcl.ACCEPTED);
			} else {
				Log.error("Publish", "publish", "impossible to publish " + new String(data) + " on topic "
						+ topic.name().toString() + " (id:" + topicId + ")");
				// TODO not used until with QoS level 1 and 2 (not implemented)
				// puback(topicId, messageId, Prtcl.REJECTED);
			}
		} else {
			Log.error("Publish", "publish", "unknown topic id: " + topicId + "-> send re-register");

			// reRegister(topicId, messageId);
			reRegister(topicId);
		}
	}

	/**
	 * @TODO not used until with QoS level 1 and 2 (not implemented)
	 *
	 * @param topicId
	 * @param messageId
	 * @param returnCode
	 *
	 *                   private void puback(final int topicId, final byte[]
	 *                   messageId, final int returnCode) {
	 *
	 *                   Log.output(device, "pub ack");
	 *
	 *                   byte[] ret = new byte[7]; ret[0] = (byte) 0x07; ret[1] =
	 *                   (byte) 0x0D; if (topicId > 255) { ret[2] = (byte) (topicId
	 *                   / 255); ret[3] = (byte) (topicId % 255); } else { ret[2] =
	 *                   (byte) 0x00; ret[3] = (byte) topicId; } ret[4] =
	 *                   messageId[0]; ret[5] = messageId[1]; ret[6] = (byte)
	 *                   returnCode;
	 *
	 *                   SerialPortWriter.write(device, ret); }
	 */
}
