package gateway.mqtt.sn.impl;

import gateway.mqtt.client.Device;
import gateway.mqtt.sn.IAction;
import gateway.utils.log.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillMessage implements IAction {

	private Device device;
	private byte[] msg;

	public WillMessage(final Device device, final byte[] msg) {

		Log.input(device, "Will message");

		this.device = device;
		this.msg = msg;
	}

	@Override
	public void exec() {
		/*
		String willmessage = new String(msg, StandardCharsets.UTF_8);
		device.mqttClient().setWillMessage(willmessage);
		*/

        Log.error("WillMessage", "exec", "NOT IMPLEMENTED YET");
	}
}
