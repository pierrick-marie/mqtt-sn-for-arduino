package gateway;

import gateway.serial.SerialPortWriter;
import utils.DeviceState;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
@Deprecated
public class MultipleSender {

	final Client client;

	public MultipleSender(final Client client) {
		this.client = client;

		send();
	}

	public void send() {


	}


}
