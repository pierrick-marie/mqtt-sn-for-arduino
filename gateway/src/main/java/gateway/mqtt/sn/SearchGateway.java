package gateway.mqtt.sn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGateway implements SnAction {

	private final Client client;
	private final Integer radius;

	public SearchGateway(final Client client, final Integer radius) {

		Log.input(client, "search gateway");

		this.client = client;
		this.radius = radius;
	}

	@Override
	public void exec() {

		Log.output(client, "gateway info");

		byte[] ret = new byte[3];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x02;
		ret[2] = (byte) Main.GATEWAY_ID;

		SerialPortWriter.write(client, ret);
	}
}
