/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.utils;

import static java.lang.System.out;

public enum Config {

	instance;

	public static String SERIAL_PORT;
	public static String IP_SERVER;
	public static Integer PORT_SERVER;

	public void parseArgs(String[] args) {

		if (args.length != 3 || args[2].equals("")) {
			out.println();
			out.println("\tMissing arguments. Usage:   SERIAL_PORT   IP_SERVER   PORT_SERVER");
			out.println();
			System.exit(-1);
		} else {
			SERIAL_PORT = args[0];
			IP_SERVER = args[1];
			PORT_SERVER = Integer.valueOf(args[2]);
		}
	}
}
