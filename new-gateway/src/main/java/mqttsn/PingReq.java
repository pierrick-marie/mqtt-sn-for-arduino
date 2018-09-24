package mqttsn;

import gateway.MultipleSender;
import utils.*;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class PingReq extends Thread {

	final Client client;
	final byte[] msg;

	public PingReq(final Client client, final byte[] msg) {

		Log.input(client, "ping request");

		this.client = client;
		this.msg = msg;
	}

	private void pingReq() {

		client.setState(utils.State.AWAKE);

		Time.sleep((long) 1000, "PingReq.pingReq()");

		sendBufferedMessage();
	}

	private void sendBufferedMessage() {

		MultipleSender multiSender = new MultipleSender(client);
		Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "Start multi-sender");
		multiSender.start();
		Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "End multi-sender");
	}

	public void run() {
		pingReq();
	}


}
