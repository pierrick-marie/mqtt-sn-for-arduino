package gateway.mqtt.sn;

import gateway.serial.SerialPortWriter;
import gateway.mqtt.client.DeviceState;
import gateway.mqtt.client.Client;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class PingReq implements SnAction {

	final Client client;
	final byte[] msg;

	public PingReq(final Client client, final byte[] msg) {

		Log.input(client, "ping request");

		this.client = client;
		this.msg = msg;
	}

	@Override
	public void exec() {
		client.setState(DeviceState.AWAKE);

		Log.debug(LogLevel.ACTIVE,"PingReq", "exec", "begin send messages");

		client.sendMqttMessages();

		Log.debug(LogLevel.ACTIVE,"PingReq", "exec", "end send messages");

		pingresp();
	}

	private void pingresp() {

		Log.output(client, "ping response");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x17;

		SerialPortWriter.write(client, ret);

		if(client.state().equals(DeviceState.AWAKE)) {
			client.setState(DeviceState.ASLEEP);
			Log.debug(LogLevel.ACTIVE,"MultipleSender", "pingResp", client + " goes to sleep");
		}
	}
}
