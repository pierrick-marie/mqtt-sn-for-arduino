/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils;

public enum Config {

	instance;

	public static int NB_XBEE_MODULE = 2;
	public static String SERIAL_PORT = "/dev/ttyUSB0";

	private static void usage() {
		System.out.println("Gateway for XBee modules - basic communication protocol");
		System.out.println("Usage:");
		System.out.println(" -p,--port      [path]   change the path of the XBee module.               Default: " + SERIAL_PORT);
		System.out.println(" -n,--modules   [num]    the number of modules supported by the gateway.   Default: " + NB_XBEE_MODULE);
		System.out.println(" -h,--help               print this message");
	}

	private static String getArgValue(final String[] args, final int index, final String message) {
		try {
			return args[index];
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(message + "missing argument value");
			usage();
			System.exit(-1);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			System.exit(-2);
		}
		return "";
	}

	public void parseArgs(String[] args) {

		for(int i = 0; i < args.length; i++) {
			switch(args[i]) {
				case "-p":
				case "--port":
					SERIAL_PORT = getArgValue(args, ++i, "Port number: ");
					break;
				case "-n":
				case "--modules":
					NB_XBEE_MODULE = Integer.parseInt(getArgValue(args, ++i, "Module number: "));
					break;
				case "-h":
				case "--help":
				default:
					usage();
					System.exit(-1);
			}
		}

		System.out.println("Serial port: " + SERIAL_PORT);
		System.out.println("Number of module: " + NB_XBEE_MODULE);
	}
}
