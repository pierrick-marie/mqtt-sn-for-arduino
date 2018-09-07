package mqttsn;

import gateway.MultipleSender;
import utils.Client;
import utils.Log;
import utils.State;
import utils.Time;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class PingReq extends Thread {

	final Client client;
	final byte[] msg;

	public PingReq(final Client client, final byte[] msg) {
		this.client = client;
		this.msg = msg;
	}

	private void pingReq() {

		Log.output(client, "Ping Request");

		client.setState(utils.State.AWAKE);

		Time.sleep((long) 1000, "PingReq.pingReq()");

		sendBufferedMessage();
	}

	private void sendBufferedMessage() {

		MultipleSender multiSender = new MultipleSender(client);
		Log.debug("PingReq", "sendBufferMessage", "Start multi-sender");
		multiSender.start();
		Log.debug("PingReq", "sendBufferMessage", "End multi-sender");
	}

	public void run() {
		pingReq();
	}


}