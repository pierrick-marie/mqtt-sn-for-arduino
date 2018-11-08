package mqtt.sn;

import gateway.Main;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Puback implements SnAction {

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

			Log.debug(LogLevel.ACTIVE, "PubAck", "puback","message id ack: " + msgID);

			client.acquitMessage(msgID);
		}
	}

	@Override
	public void exec() {
		puback();
	}
}