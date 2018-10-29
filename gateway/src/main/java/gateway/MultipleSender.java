package gateway;

import gateway.serial.SerialPortWriter;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MultipleSender extends Thread {

	final Client client;

	public MultipleSender(final Client client) {
		this.client = client;
	}

	public void run() {

		// DEBUG
		Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "Begin Multiple Sender");

		for (MqttMessage mqttMessage : client.mqttMessages) {

			// DEBUG
			Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "Starting to send mqttMessage " + mqttMessage);

			Sender sender = new Sender(client, mqttMessage);

			// DEBUG
			Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "Start sender");
			sender.start();
			try {
				sender.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "End sender");
		}

		client.mqttMessages.clear();

		Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "End of multi-sender");

		pingresp();
	}

	private void pingresp() {

		Log.output(client, "ping response");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x03;
		ret[1] = (byte) 0x17;

		SerialPortWriter.write(client, ret);

		if(client.state().equals(utils.State.AWAKE)) {
			client.setState(utils.State.ASLEEP);
			Log.debug(LogLevel.ACTIVE,"MultipleSender", "pingResp", client + " goes to sleep");
			if(0 != client.duration()) {
				TimeOut timeOut = new TimeOut(client, client.duration());
				Threading.thread(timeOut, false);
			}
		}
	}
}
