/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.Main;
import gateway.mqtt.client.Device;
import gateway.serial.Writer;
import gateway.utils.log.Log;

public class SearchGateway implements Runnable {

	private final Device device;
	// NOT IMPLEMENTED YET
	// private final Integer radius;

	public SearchGateway(final Device device, final Integer radius) {

		Log.xbeeInput(device, "search gateway");

		this.device = device;
		// NOT IMPLEMENTED YET
		// this.radius = radius;
	}

	@Override
	public void run() {

		Log.info(device + " first connect OK");
		Log.xbeeOutput(device, "gateway info");

		final byte[] ret = new byte[3];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x02;
		ret[2] = (byte) Main.GATEWAY_ID;

		Writer.Instance.write(device, ret);
	}
}
