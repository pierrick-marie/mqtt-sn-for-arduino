/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 *                           on 20/01/2023
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
	private LogLevel logLevel = LogLevel.NONE;
	private Integer portServer;
	private String serialPort;

	private void usage() {
		Log.info("\nMQTT-SN Gateway for XBee modules \nUsage:   SERIAL_PORT   IP_SERVER   PORT_SERVER   [OPTION LOG_LEVEL: (ACTIVE || VERBOSE) DEFAULT: NONE]\n");
	}

	private void error() {
		usage();
		System.exit(-1);
	}

	public String ipServer() {
		return ipServer;
	}

	public LogLevel logLevel() {
		return logLevel;
	}

	public Integer portServer() {
		return portServer;
	}

	public String serialPort() {
		return serialPort;
	}

	private void ok() {
		Log.info("Starting the gateway \n * Serial: " + Config.Instance.serialPort() + "\n * IP server: "
				+ Config.Instance.ipServer() + " \n * Port server: " + Config.Instance.portServer()
				+ " \n * Log level: " + Config.Instance.logLevel());
	}

	public void parseArgs(final String[] args) {

		if( args.length < NB_ARGS_MIN ) {
			error();
		}

		if( args.length > NB_ARGS_MAX ) {
			error();
		}

		serialPort = args[SERIAL_PORT];
		ipServer = args[IP_SERVER];
		portServer = Integer.valueOf(args[PORT_SERVER]);
		
		if( args.length == NB_ARGS_MAX ) {
			logLevel = LogLevel.valueOf(args[LOG_LEVEL]);
		}

		ok();
	}
}
