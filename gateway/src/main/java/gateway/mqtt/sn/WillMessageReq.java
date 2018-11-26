package gateway.mqtt.sn;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillMessageReq implements SnAction {

	private final Device device;

	public WillMessageReq(final Device device) {
		this.device = device;
	}

	@Override
	public void exec() {

		Log.input(device, "Will DMqMessage Req");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x08;

		// SerialPortWriter.write(device, ret);

	}
}
