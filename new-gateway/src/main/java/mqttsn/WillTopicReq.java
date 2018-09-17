package mqttsn;

import gateway.serial.SerialPortWriter;
import utils.Client;
import utils.Log;
import utils.LogLevel;
import utils.Time;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillTopicReq extends Thread {


	private final Client client;

	public WillTopicReq(final Client client) {
		this.client = client;
	}

	private void willTopicReq() {

		Log.input(client, "Will Topic Req");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x06;

		client.setWillTopicReq(true);

		SerialPortWriter.write(client, ret);

		int cpt = 0;
		while (cpt < 10 && client.willTopicReq()) {
			Time.sleep((long) 1000, "WillTopicReq.willtopicreq()");
			cpt++;
		}

		if (client.willTopicReq()) {
			Log.debug(LogLevel.ACTIVATED,"WillTopicReq", "willTopicReq","Resend Will Topic Req");
			willTopicReq();
		}
	}

	public void run() {
		willTopicReq();
	}
}
