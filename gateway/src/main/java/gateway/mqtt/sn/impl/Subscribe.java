package gateway.mqtt.sn.impl;

import gateway.mqtt.sn.IAction;
import gateway.serial.SerialPortWriter;
import gateway.mqtt.impl.Topic;
import gateway.mqtt.client.Device;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Subscribe implements IAction {

	final Device device;
	final byte[] msg;

	public Subscribe(final Device device, final byte[] msg) {

		Log.input(device, "subscribe");

		this.device = device;
		this.msg = msg;
	}

	public void subscribe() {

		byte flags = msg[0];
		byte[] messageId = new byte[2];
		messageId[0] = msg[1];
		messageId[1] = msg[2];

		if (null == device.mqttClient() || !device.mqttClient().isConnected()) {
			Log.error("Subscribre", "subscribe", device + "is not connected");
			suback(new byte[]{(byte) Prtcl.DEFAULT_QOS}, messageId, 0, Prtcl.REJECTED);
			return;
		}

		byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++) {
			name[i] = msg[3 + i];
		}
		String topicName = new String(name, StandardCharsets.UTF_8);

		synchronized (device.Topics) {

			Topic topic = device.Topics.get(topicName);

			if (null != topic) {
				if (!topic.isSubscribed()) {
				    if(device.mqttClient().subscribe(topic)){
						Log.debug(LogLevel.ACTIVE, "Subscribe", "subscribe", "subcription ok -> sending sub ack message");
					} else {
						Log.error("Subscribre", "subscribe", "imposible to subscribe to the topic: " + topicName);
						suback(new byte[]{(byte) Prtcl.DEFAULT_QOS}, messageId, topic.id(), Prtcl.REJECTED);
						return;
					}
				}
				Log.debug(LogLevel.ACTIVE, "Subscribe", "subscribe", "Topics " + topicName + " is already registered with id: " + topic.id());
				suback(new byte[]{(byte) Prtcl.DEFAULT_QOS}, messageId, topic.id(), Prtcl.ACCEPTED);
			} else {
				Log.error("Subscribe", "subscribe", "Topics NOT registered " + device.Topics.get(1));
				// Error - topicId = -1
				suback(new byte[]{(byte) Prtcl.DEFAULT_QOS}, messageId, -1, Prtcl.REJECTED);
			}
		}
	}

	private void suback(final byte[] qos, final byte[] messageId, final int topicId, final byte returnCode) {

		Log.output(device, "sub ack");

		byte[] ret = new byte[8];
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

	@Override
	public void exec() {
		subscribe();
	}
}
