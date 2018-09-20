package mqttsn;

import gateway.MultipleSender;
import utils.*;
import utils.log.Log;
import utils.log.LogLevel;

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
		Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "Start multi-sender");
		multiSender.start();
		Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "End multi-sender");
	}

	public void run() {
		pingReq();
	}


}
