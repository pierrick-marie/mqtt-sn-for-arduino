/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import gateway.mqtt.client.Device;
import gateway.utils.Config;

public class Log {

	private static Boolean COLOR = true;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final String XBEE_INPUT = " ---  XBEE  >>> ";
	private static final String XBEE_OUTPUT = " <<<  XBEE  --- ";
	private static final String BROKER_INPUT = " --- BROKER >>> ";
	private static final String BROKER_OUTPUT = " <<< BROKER --- ";

	private final static gateway.utils.log.LogLevel LEVEL = Config.Instance.logLevel();

	private static void bBlue(final String message) {
		if (COLOR) {
			System.out.print("\033[34;1m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	private static void bRed(final String message) {
		if (COLOR) {
			System.out.print("\033[31;1m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	synchronized public static void brokerInput(final Device device, final String message) {
		if (LEVEL.ordinal() >= LogLevel.ACTIVE.ordinal()) {
			bBlue(" [ " + BROKER_INPUT + " ]  ");
			System.out.println(message + " for " + device);
		}
	}

	synchronized public static void brokerOutput(final Device device, final String message) {
		if (LEVEL.ordinal() >= LogLevel.ACTIVE.ordinal()) {
			bBlue(" [ " + BROKER_OUTPUT + " ]  ");
			System.out.println(message + " from " + device);
		}
	}

	private static void bYellow(final String message) {
		if (COLOR) {
			System.out.print("\033[33;1m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	synchronized private static void debug(final gateway.utils.log.LogLevel level, final String className,
			final String methodeName, final String message) {
		if (LEVEL.ordinal() >= level.ordinal()) {

			final Date date = new Date();
			bYellow(" # [  DEBUG " + dateFormat.format(date) + "  ] ");

			yellow(className + ".");
			yellow(methodeName + ": ");
			yellow(message + "\n");
		}
	}

	synchronized public static void debug(final String message) {
		if (LEVEL.ordinal() >= LogLevel.VERBOSE.ordinal()) {
			final Date date = new Date();
			bYellow(" # [  DEBUG " + dateFormat.format(date) + "  ] ");
			yellow(message + "\n");
		}
	}

	synchronized public static void debug(final String className, final String methodeName, final String message) {
		debug(LogLevel.VERBOSE, className, methodeName, message);
	}

	synchronized public static void error(final String className, final String methodeName, final String message) {

		final Date date = new Date();
		bRed(" [  ERROR " + dateFormat.format(date) + "  ] ");
		red(className + ".");
		red(methodeName + ": ");
		red(message + "\n");
	}

	synchronized public static void info(final String message) {
		// bBlue(" * [ --- INFO --- ] ");
		// System.out.println(message);
		System.out.println("\033[1m" + message + "\033[0m");
	}

	synchronized public static void print(final byte[] data) {

		if (LEVEL.ordinal() >= LogLevel.VERBOSE.ordinal()) {
			debug("Print buffer");

			for (final byte element : data) {
				System.out.print(String.format("%02X ", element));
			}

			System.out.println("");
		}
	}

	private static void red(final String message) {
		if (COLOR) {
			System.out.print("\033[31m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	synchronized public static void xbeeInput(final Device device, final String message) {

		if (LEVEL.ordinal() >= LogLevel.ACTIVE.ordinal()) {
			if (null == device) {
				bBlue(" [ " + XBEE_INPUT + " ]  ");
				System.out.println(message);
			} else {
				bBlue(" [ " + XBEE_INPUT + " ]  ");
				System.out.println(message + " from " + device);
			}
		}
	}

	synchronized public static void xbeeOutput(final Device device, final String message) {
		if (LEVEL.ordinal() >= LogLevel.ACTIVE.ordinal()) {
			bBlue(" [ " + XBEE_OUTPUT + " ]  ");
			System.out.println(message + " to " + device);
		}
	}

	private static void yellow(final String message) {
		if (COLOR) {
			System.out.print("\033[33m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	/*
	 * public Log() { if (Config.LOG_LEVEL.equals(LogLevel.ACTIVE.name())) { LEVEL =
	 * LogLevel.ACTIVE; } else { LEVEL = LogLevel.NONE; } }
	 */
}