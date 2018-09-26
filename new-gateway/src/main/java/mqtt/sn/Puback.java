package mqtt.sn;

import gateway.Main;
import utils.client.Client;
import utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Puback extends Thread {

	final Client client;
	final byte[] msg;

	public Puback(final Client client, final byte[] msg) {

		Log.input(client, "pub ack");

		this.client = client;
		this.msg = msg;
	}

	private void puback() {

		if (msg[4] == (byte) 0x00) {
			int msgID = (msg[3] << 8) + (msg[2] & 0xFF);
			Main.MessageIdAck.add(msgID);
		}
	}

	public void run() {
		puback();
	}


}
