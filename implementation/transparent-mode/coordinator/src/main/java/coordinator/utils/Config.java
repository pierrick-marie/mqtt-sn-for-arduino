/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package coordinator.utils;

public enum Config {

	instance;

	public static String SERIAL_PORT = "/dev/ttyUSB0";

	public void parseArgs(String[] args) {

		if (1 == args.length) {
			SERIAL_PORT = args[0];
		}
	}
}
