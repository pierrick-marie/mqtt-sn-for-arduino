/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

/**
 * @TODO not implemented yet
 */
public class WillTopicReq implements Runnable {

	private final Device device;

	public WillTopicReq(final Device device) {
		this.device = device;
	}

	@Override
	public void run() {
		willTopicReq();
	}

	private void willTopicReq() {

		Log.input(device, "Will Topics Req");

		final byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x06;

		// SerialPortWriter.write(device, ret);
	}
}
