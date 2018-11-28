package gateway.mqtt.sn.impl;

import gateway.Main;
import gateway.mqtt.sn.IAction;
import gateway.serial.SerialPortWriter;
import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGateway implements IAction {

	private final Device device;
	private final Integer radius;

	public SearchGateway(final Device device, final Integer radius) {

		Log.input(device, "search gateway");

		this.device = device;
		this.radius = radius;
	}

	@Override
	public void exec() {

		Log.output(device, "gateway info");

		byte[] ret = new byte[3];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x02;
		ret[2] = (byte) Main.GATEWAY_ID;

		SerialPortWriter.write(device, ret);
	}
}
