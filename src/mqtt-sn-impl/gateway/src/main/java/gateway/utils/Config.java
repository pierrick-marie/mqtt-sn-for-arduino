/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils;

import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public enum Config {

	Instance;

	private final int NB_ARGS_MIN = 3;
	private final int NB_ARGS_MAX = 4;

	private final Integer SERIAL_PORT = 0;
	private final Integer IP_SERVER = 1;
	private final Integer PORT_SERVER = 2;
	private final Integer LOG_LEVEL = 3;

	private String ipServer;
	private LogLevel logLevel;
	private Integer portServer;
	private String serialPort;

	private void error() {
		Log.info("");
		Log.info("Missing arguments. Usage:   SERIAL_PORT   IP_SERVER   PORT_SERVER   [OPTION LOG_LEVEL: (ACTIVE || VERBOSE)]");
		Log.info("");
		System.exit(-1);
	}

	public String ipServer() {
		return ipServer;
	}

	public LogLevel logLevel() {
		return logLevel;
	}

	private void ok() {
		Log.info("Starting the gateway \n * Serial: " + Config.Instance.serialPort() + "\n * IP server: "
				+ Config.Instance.ipServer() + " \n * Port server: " + Config.Instance.portServer()
				+ " \n * Log level: " + Config.Instance.logLevel());
	}

	public void parseArgs(final String[] args) {

		switch (args.length) {
		case NB_ARGS_MIN:

			logLevel = LogLevel.NONE;
			break;

		case NB_ARGS_MAX:

			try {
				if (args[LOG_LEVEL].isEmpty()) {
					logLevel = LogLevel.NONE;
				} else {
					logLevel = LogLevel.valueOf(args[LOG_LEVEL]);
				}
			} catch (final IllegalArgumentException exception) {
				error();
			}
			break;

		default:

			error();
			return;
		}

		serialPort = args[SERIAL_PORT];
		ipServer = args[IP_SERVER];
		portServer = Integer.valueOf(args[PORT_SERVER]);
		ok();
	}

	public Integer portServer() {
		return portServer;
	}

	public String serialPort() {
		return serialPort;
	}
}
