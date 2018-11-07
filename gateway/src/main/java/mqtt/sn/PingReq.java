package mqtt.sn;

import gateway.MultipleSender;
import utils.*;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class PingReq implements SnAction {

	final Client client;
	final byte[] msg;

	public PingReq(final Client client, final byte[] msg) {

		Log.input(client, "ping request");

		this.client = client;
		this.msg = msg;
	}

	private void pingReq() {

		client.setState(DeviceState.AWAKE);

		// TODO: DEBUG?
		// Time.sleep((long) 1000, "PingReq.pingReq()");

		MultipleSender multiSender = new MultipleSender(client);
		Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "Start multi-sender");
		multiSender.start();
	}

	@Override
	public void exec() {
		pingReq();
	}
}
