package gateway.mqtt.sn;

import gateway.mqtt.client.Device;
import gateway.serial.SerialPortWriter;
import gateway.mqtt.SnTopic;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Publish implements SnAction {

	private final Device device;
	private final byte[] msg;

	public Publish(final Device device, final byte[] msg) {

		Log.input(device, "publish");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void exec() {

		byte flags = msg[0];
		// @TODO not implemented yet
		// QoS qos = Prtcl.DEFAUlT_QOS;

		int topicId = (msg[2] << 8) + (msg[1] & 0xFF);

		byte[] messageId = new byte[2];
		messageId[0] = msg[3];
		messageId[1] = msg[4];

		if (null == device.mqttClient() || !device.mqttClient().isConnected()) {
			Log.error("Publish", "publish", device + "is not connected");
			// @TODO not used until with QoS level 1 and 2 (not implemented)
			// puback(topicId, messageId, Prtcl.REJECTED);
			return;
		}

		byte[] data = new byte[msg.length - 5];
		for (int i = 0; i < data.length; i++) {
			data[i] = msg[5 + i];
		}

		if (device.Topics.contains(topicId)) {

			SnTopic topic = device.Topics.get(topicId);

			if( device.mqttClient().publish(topic, new String(data)) ) {
				Log.debug(LogLevel.ACTIVE, "Publish", "publish", "published "
													 + new String(data) + " on topic "
													 + topic.name().toString() + " (id:" + topicId
													 + ")");
				// @TODO not used until with QoS level 1 and 2 (not implemented)
				// puback(topicId, messageId, Prtcl.ACCEPTED);
			} else {
				Log.error("Publish", "publish", "impossible to publish "
													 + new String(data) + " on topic "
													 + topic.name().toString() + " (id:" + topicId
													 + ")");
				// @TODO not used until with QoS level 1 and 2 (not implemented)
				// puback(topicId, messageId, Prtcl.REJECTED);
			}
		} else {
			Log.error("Publish", "publish", "unknown topic id: " + topicId
												 + "-> send re-register");
			reRegister(topicId, messageId);
		}
	}

	private void reRegister(final int topicId, final byte[] messageId) {

		Log.output(device, "re register");

		byte[] ret = new byte[7];
		ret[0] = (byte) 0x07;
		ret[1] = (byte) 0x1E;
		if (topicId > 255) {
			ret[2] = (byte) (topicId % 255);
			ret[3] = (byte) (topicId / 255);
		} else {
			ret[2] = (byte) topicId;
			ret[3] = (byte) 0x00;
		}
		ret[4] = messageId[0];
		ret[5] = messageId[1];
		ret[6] = Prtcl.ACCEPTED;

		SerialPortWriter.write(device, ret);
	}

	/**
	 * @TODO not used until with QoS level 1 and 2 (not implemented)
	 *
	 * @param topicId
	 * @param messageId
	 * @param returnCode
	 */
	private void puback(final int topicId, final byte[] messageId, final int returnCode) {

		Log.output(device, "pub ack");

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

		SerialPortWriter.write(device, ret);
	}
}
