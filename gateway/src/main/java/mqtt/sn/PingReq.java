package mqtt.sn;

import gateway.MultipleSender;
import gateway.Threading;
import gateway.TimeOut;
import gateway.serial.SerialPortWriter;
import utils.*;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

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

		// @DEPRECATED
		// MultipleSender multiSender = new MultipleSender(client);
		// Log.debug(LogLevel.ACTIVE,"PingReq", "sendBufferMessage", "Start multi-sender");

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
			if(0 != client.duration()) {
				TimeOut timeOut = new TimeOut(client, client.duration());
				Threading.thread(timeOut, false);
			}
		}
	}
}
