package utils;

import gateway.Main;
import org.fusesource.mqtt.client.QoS;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Utils {

	public static byte[] getTopicId(final String name) {

		byte[] ret = new byte[2];
		int id = Main.TopicName.get(name);

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

	public static QoS getQoS(final int qos) {

		switch(qos) {
			case 0:
				return QoS.AT_MOST_ONCE;
			case 1:
				return QoS.AT_LEAST_ONCE;
			case 2:
				return QoS.EXACTLY_ONCE;
			default:
				return QoS.AT_LEAST_ONCE;
		}

	}
}
