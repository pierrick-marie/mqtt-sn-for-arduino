package mqttsn;

import gateway.serial.SerialPortWriter;
import utils.Client;
import utils.Log;
import utils.LogLevel;
import utils.Time;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessageReq extends Thread {

	private final Client client;

	public WillMessageReq(final Client client) {
		this.client = client;
	}

	private void willMessagerReq() {

		Log.input( client, "Will Message Req");

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
			Log.debug(LogLevel.ACTIVATED,"WillMessageReq", "willMessageReq","Resend Will Message Req");
			willMessagerReq();
		}
	}

	public void run() {
		willMessagerReq();
	}
}
