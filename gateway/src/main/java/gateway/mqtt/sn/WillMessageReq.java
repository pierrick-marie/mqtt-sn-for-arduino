package gateway.mqtt.sn;

import gateway.mqtt.client.Client;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillMessageReq implements SnAction {

	private final Client client;

	public WillMessageReq(final Client client) {
		this.client = client;
	}

	@Override
	public void exec() {

		Log.input(client, "Will MqttMessage Req");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x08;

		// SerialPortWriter.write(client, ret);

	}
}
