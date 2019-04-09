/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway;

import gateway.serial.SerialPortReader;
import gateway.utils.Config;
import gateway.utils.log.Log;

public class Main {

	public static final int GATEWAY_ID = 1;

	public static void main(String[] args) {

		Log.COLOR = false;

		Config.instance.parseArgs(args);

		Log.print("Starting the gateway \n * Serial: " + Config.SERIAL_PORT + "\n * IP server: " + Config.IP_SERVER
				+ " \n * Port server: " + Config.PORT_SERVER);

		new SerialPortReader();
	}
}
