package mqtt.sn;

import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not used until with QoS level 1 and 2 (not implemented)
 */
public class Puback implements SnAction {

	final Client client;
	final byte[] msg;

	public Puback(final Client client, final byte[] msg) {

		Log.input(client, "pub ack");

		this.client = client;
		this.msg = msg;
	}

	@Override
	public void exec() {

		if (msg[4] == (byte) 0x00) {
			int msgID = (msg[3] << 8) + (msg[2] & 0xFF);

			Log.debug(LogLevel.ACTIVE, "PubAck", "puback","message id ack: " + msgID);

			client.acquitMessage(msgID);
		}
	}



}
