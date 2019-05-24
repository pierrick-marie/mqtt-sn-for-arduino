/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.utils;

import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public enum Config {

	Instance;

	private final Integer NB_ARGS = 4;
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
		Log.info("Missing arguments. Usage:   SERIAL_PORT   IP_SERVER   PORT_SERVER   LOG_LEVEL(NONE || ACTIVE)");
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

	public void parseArgs(String[] args) {

		if (args.length != NB_ARGS) {
			error();
		} else {

			try {
				logLevel = LogLevel.valueOf(args[LOG_LEVEL]);
			} catch (final IllegalArgumentException exception) {
				error();
			}

			serialPort = args[SERIAL_PORT];
			ipServer = args[IP_SERVER];
			portServer = Integer.valueOf(args[PORT_SERVER]);

			ok();
		}
	}

	public Integer portServer() {
		return portServer;
	}

	public String serialPort() {
		return serialPort;
	}
}
