package mqttsn;

import gateway.Main;
import org.fusesource.mqtt.client.MQTT;
import utils.Utils;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillTopic extends Thread {

	byte[] add64;
	byte[] add16;
	byte[] msg;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public WillTopic(byte[] add64, byte[] add16, byte[] msg) {
		this.add64 = add64;
		this.add16 = add16;
		this.msg = msg;
	}

	public void willtopic() {
		Date date = new Date();

		// @TODO DEBUG
		// System.out.println(sdf.format(date)+": -> "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Willtopic");

		// @TODO DEBUG
		// Main.WillTopicAck.put(Utils.byteArrayToString(add64), false);

		if (msg.length == 0) {

			// @TODO DEBUG
			// String clientID=Main.AddressClientMap.get(Utils.byteArrayToString(add64));
			// MQTT mqtt=Main.ClientMap.get(clientID);
			// mqtt.setWillTopic("");
			// mqtt.setWillMessage("");
		} else {
			byte flags = msg[0];
			int will_QOS = flags & 0b01100000 >> 5;
			//System.out.println(will_QOS);
			boolean will_retain = (flags & 0b00010000) == 1;
			byte[] data = new byte[msg.length - 1];
			for (int i = 0; i < msg.length - 1; i++)
				data[i] = msg[i + 1];
			String willtopic = new String(data, StandardCharsets.UTF_8);

			// @TODO DEBUG
			// String clientID = Main.AddressClientMap.get(Utils.byteArrayToString(add64));
			// MQTT mqtt = Main.ClientMap.get(clientID);
			// mqtt.setWillTopic(willtopic);
			// mqtt.setWillQos(Utils.getQoS(will_QOS));
			// mqtt.setWillRetain(will_retain);
		}
	}

	public void run() {
		willtopic();
	}
}