package gateway;

import gateway.serial.SerialPortReader;
import utils.log.Log;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

	public static final int GATEWAY_ID = 1;

	public static final String SERIAL_PORT = "/dev/ttyUSB0";

	public static void main(String[] args) {

		new SerialPortReader();

		Log.print("Starting the gateway, waiting for connections...");
	}
}

