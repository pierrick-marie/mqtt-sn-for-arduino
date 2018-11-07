package mqtt.sn;

import gateway.serial.SerialPortWriter;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;
import utils.Time;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessageReq implements SnAction {

	private final Client client;

	public WillMessageReq(final Client client) {
		this.client = client;
	}

	private void willMessagerReq() {

		Log.input( client, "Will MqttMessage Req");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x08;

		client.setWillTopicReq(true);

		SerialPortWriter.write(client, ret);

		int cpt = 0;
		while (cpt < 10 && client.willMessageReq()) {

			Time.sleep((long) 1000, "WillMessageAck.willMessageReq()");
			cpt++;
		}


		if (client.willMessageReq()) {
			Log.debug(LogLevel.ACTIVE,"WillMessageReq", "willMessageReq","Resend Will MqttMessage Req");
			willMessagerReq();
		}
	}

	@Override
	public void exec() {
		willMessagerReq();
	}
}
