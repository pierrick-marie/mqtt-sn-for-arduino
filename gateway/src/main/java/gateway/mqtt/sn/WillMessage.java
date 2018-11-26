package gateway.mqtt.sn;

import gateway.mqtt.client.Device;
import gateway.utils.log.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 *
 * @TODO not implemented yet
 */
public class WillMessage implements SnAction {

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
