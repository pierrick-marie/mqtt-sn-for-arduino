package mqttsn;

import gateway.Main;
import gateway.serial.SerialPortWriter;
import utils.Client;
import utils.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGateway extends Thread {

	private final Client client;
	private final Integer radius;

	public SearchGateway(final Client client, final Integer radius) {

		this.client = client;
		this.radius = radius;
	}

	private void searchGateway() {

		Log.input(client, "Search gateway");

		byte[] ret = new byte[3];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x02;
		ret[2] = (byte) Main.GatewayId;

		SerialPortWriter.write(client, ret);
	}

	public void run() {
		searchGateway();
	}
}