package mqtt.sn;

import gateway.serial.SerialPortWriter;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;
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

		Log.input(client, "Will Topics Req");

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
			Log.debug(LogLevel.ACTIVE,"WillTopicReq", "willTopicReq","Resend Will Topics Req");
			willTopicReq();
		}
	}

	public void run() {
		willTopicReq();
	}
}
