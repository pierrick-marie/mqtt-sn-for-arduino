package mqttsn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;
import utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Subscribe extends Thread {

	final Client client;
	final byte[] msg;

	public Subscribe(final Client client, final byte[] msg) {

		Log.input(client, "subscribe");

		this.client = client;
		this.msg = msg;
	}

	public void subscribe() {

		byte flags = msg[0];
		boolean DUP = (flags & 0b10000000) == 1;
		int qos = (flags & 0b01100000) >> 5;
		int topicIDType = flags & 0b00000011;
		byte[] messageId = new byte[2];
		messageId[0] = msg[1];
		messageId[1] = msg[2];

		if (!client.mqttClient().isConnected()) {
			Log.error("Subscribre", "subscribe", client + "is not connected");
			suback(new byte[]{(byte)QoS.AT_LEAST_ONCE.ordinal()}, messageId, 0, false);
			return;
		}

		byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++) {
			name[i] = msg[3 + i];
		}
		String topicName = new String(name, StandardCharsets.UTF_8);
		int topicId;

		if (Main.TopicName.containsKey(topicName)) {

			topicId = Main.TopicName.get(topicName);

			Log.debug(LogLevel.ACTIVE,"Subscribe", "subscribe", "Topic " + topicName + " is registered with final id: " + topicId);

			try {
				client.mqttClient().subscribe(topicName);
				Log.debug(LogLevel.ACTIVE,"Subscribe", "subscribe", "subcription ok -> sending sub ack message");
				suback(new byte[]{(byte)QoS.AT_LEAST_ONCE.ordinal()}, messageId, 0, true);
				/**
				 * TODO: handle the received messages!
				 */
			} catch (TimeoutException e) {
				Log.error("Subscribre", "subscribe", "imposible to subscribe to the topic: " + topicName);
				Log.debug(LogLevel.VERBOSE, "Subscribre", "subscribe", e.getMessage());
			}
		} else {
			Log.error("Subscribe", "subscribe", "Topic NOT registered");
			suback(new byte[]{(byte)QoS.AT_LEAST_ONCE.ordinal()}, messageId, 0, false);
			return;
		}
	}

	private void suback(final byte[] qoses, final byte[] msgID, final int topicID, final boolean ok) {

		Log.output(client, "sub ack");

		byte[] ret = new byte[8];
		ret[0] = (byte) 0x08;
		ret[1] = (byte) 0x13;
		ret[2] = qoses[0];
		if (topicID > 255) {
			ret[3] = (byte) (topicID / 255);
			ret[4] = (byte) (topicID % 255);
		} else {
			ret[3] = (byte) 0x00;
			ret[4] = (byte) topicID;
		}
		ret[5] = msgID[0];
		ret[6] = msgID[1];
		if (ok) {
			ret[7] = (byte) 0x00;
		} else {
			ret[7] = (byte) 0x02;
		}

		SerialPortWriter.write(client, ret);
	}

	public void run() {
		subscribe();
	}


}
