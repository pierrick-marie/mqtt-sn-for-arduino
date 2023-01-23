/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils;

public enum Config {

	instance;

	public static String SERIAL_PORT = "/dev/ttyUSB0";

	public void parseArgs(String[] args) {

		if (1 == args.length) {
			System.out.println("Using port: " + args[0]);
			SERIAL_PORT = args[0];
		} else {
			System.out.println("Using default port: " + SERIAL_PORT);
		}
	}
}
