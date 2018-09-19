package gateway;

import gateway.serial.SerialPortWriter;
import utils.*;

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
		Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "Begin Multiple Sender: " + client.messages);

		for (Message message : client.messages) {

			// DEBUG
			Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "Starting to send message " + message);

			Sender sender = new Sender(client, message);

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

		client.messages.clear();

		Log.debug(LogLevel.ACTIVE,"MultiSender", "run", "End of multi-sender");


		pingresp();
	}

	private void pingresp() {

		Log.input(client, "Ping Response");

		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
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
