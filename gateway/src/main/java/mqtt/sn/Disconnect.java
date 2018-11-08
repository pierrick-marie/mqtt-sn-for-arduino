package mqtt.sn;

import gateway.Threading;
import gateway.TimeOut;
import gateway.serial.SerialPortWriter;
import utils.DeviceState;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Disconnect implements SnAction {

	private final Client client;
	private final byte[] msg;

	public Disconnect(final Client client, final byte[] msg) {

		Log.input(client, "disconnect");

		this.client = client;
		this.msg = msg;
	}

	private void disconnect() {

		if (msg.length == 4) {
			int duration = (msg[0] << 8) + (msg[1] & 0xFF);
			//System.out.println("Duration: "+duration);
			if (duration > 0) {

				client.setState(DeviceState.ASLEEP).setDuration(duration);

				disconnectAck();

				Log.debug(LogLevel.ACTIVE,"Disconnect", "diconnect", "Going into sleep");

				TimeOut timeOut = new TimeOut(client, duration);
				Threading.thread(timeOut, false);
			}
		} else {

			// @TODO real disconnect
			client.setState(DeviceState.DISCONNECTED);
			disconnectAck();
		}
	}

	private void disconnectAck() {

		Log.input(client, "Disconnect Ack");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x18;

		SerialPortWriter.write(client, ret);
	}

	@Override
	public void exec() {
		disconnect();
	}
}