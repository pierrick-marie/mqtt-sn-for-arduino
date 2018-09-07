package mqttsn;

import gateway.Main;
import utils.Client;
import utils.Log;
import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Puback extends Thread {

	final Client client;
	final byte[] msg;

	public Puback(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	private void puback() {

		Log.output(client, "PubAck Client");

		if (msg[4] == (byte) 0x00) {
			int msgID = (msg[3] << 8) + (msg[2] & 0xFF);
			Main.MessageIdAck.add(msgID);
		}
	}

	public void run() {
		puback();
	}


}
