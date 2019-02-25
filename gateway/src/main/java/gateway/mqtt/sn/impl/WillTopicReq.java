/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.sn.IAction;
import gateway.utils.log.Log;

/**
 * @TODO not implemented yet
 */
public class WillTopicReq implements IAction {

	private final Device device;

	public WillTopicReq(final Device device) {
		this.device = device;
	}

	private void willTopicReq() {

		Log.input(device, "Will Topics Req");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x06;

		// SerialPortWriter.write(device, ret);
	}

	@Override
	public void exec() {
		willTopicReq();
	}
}
