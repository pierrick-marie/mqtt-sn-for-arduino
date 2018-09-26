package mqtt.sn;

import org.fusesource.mqtt.client.QoS;

public class Prtcl {

	public static final byte ACCEPTED = 0x00;
	public static final byte REJECTED = 0x03;

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
