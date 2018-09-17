package mqttsn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Topic;
import utils.Client;
import utils.Log;
import utils.LogLevel;
import utils.Utils;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Subscribe extends Thread {

	final Client client;
	final byte[] msg;

	public Subscribe(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	public void subscribe() {

		Log.output(client, "Subscribe");

		byte flags = msg[0];
		boolean DUP = (flags & 0b10000000) == 1;
		int qos = (flags & 0b01100000) >> 5;
		int topicIDType = flags & 0b00000011;
		byte[] msgID = new byte[2];
		msgID[0] = msg[1];
		msgID[1] = msg[2];
		byte[] name = new byte[msg.length - 3];
		for (int i = 0; i < msg.length - 3; i++)
			name[i] = msg[3 + i];
		String topicName = new String(name, StandardCharsets.UTF_8);
		int topicID;

		if (Main.TopicName.containsKey(topicName)) {
			topicID = Main.TopicName.get(topicName);

			Topic[] topics = {new Topic(topicName, Utils.getQoS(qos))};
			final int finalTopicID = topicID;

			client.connection().subscribe(topics, new Callback<byte[]>() {
				@Override
				public void onSuccess(byte[] value) {
					client.messages.clear();
					suback(value, msgID, finalTopicID);
				}

				@Override
				public void onFailure(Throwable e) {
					Log.error("Subscribe", "subscribe", "Error on subscribe");
					Log.debug(LogLevel.ACTIVATED,"Subscribe", "subscribe", e.getMessage());
				}
			});

		} else {
			Log.error("Subscribe", "subscribe", "Topic NOT registered");
		}
	}

	private void suback(final byte[] qoses, final byte[] msgID, final int topicID) {

		Log.input(client, "Suback");

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
		ret[7] = (byte) 0x00;

		SerialPortWriter.write(client, ret);
	}

	public void run() {
		subscribe();
	}


}
