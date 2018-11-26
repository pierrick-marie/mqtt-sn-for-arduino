package gateway.mqtt.sn;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillTopicReq implements SnAction {


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
