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
public class WillTopic implements IAction {

	// private Device device;
	// private byte[] msg;

	public WillTopic(final Device device, final byte[] msg) {

		Log.input(device, "Will topic");

		// this.device = device;
		// this.msg = msg;
	}

	@Override
	public void exec() {
		/*
		 * if (msg.length == 0) {
		 * 
		 * device.mqttClient().setWillMessage(""); device.mqttClient().setWillTopic("");
		 * 
		 * } else {
		 * 
		 * byte flags = msg[0]; boolean will_retain = (flags & 0b00010000) == 1; byte[]
		 * data = new byte[msg.length - 1];
		 * 
		 * for (int i = 0; i < msg.length - 1; i++) { data[i] = msg[i + 1]; }
		 * 
		 * String willtopic = new String(data, StandardCharsets.UTF_8);
		 * 
		 * device.mqttClient().setWillTopic(willtopic);
		 * device.mqttClient().setWillQos(Prtcl.DEFAUlT_QOS);
		 * device.mqttClient().setWillRetain(will_retain); }
		 */

		Log.error("WillTopic", "exec", "NOT IMPLEMENTED YET");
	}
}
