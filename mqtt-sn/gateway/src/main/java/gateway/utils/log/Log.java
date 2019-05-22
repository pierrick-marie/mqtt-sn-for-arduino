/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.utils.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import gateway.mqtt.client.Device;

public class Log {

	public static Boolean COLOR = true;
	public static final Boolean ERROR = true;
	public static final gateway.utils.log.LogLevel LEVEL = gateway.utils.log.LogLevel.VERBOSE;

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final String INPUT = "received message - ";
	private static final String OUTPUT = "send message - ";

	synchronized public static void activeDebug(final String message) {
		bYellow(" # [ " + gateway.utils.log.LogLevel.ACTIVE.name() + " ] ");
		yellow(message + "\n");
	}

	private static void bBlue(final String message) {
		if (COLOR) {
			System.out.print("\033[34;1m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	private static void blue(final String message) {
		if (COLOR) {
			System.out.print("\033[34m" + message + "\033[0m");
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

	private static void bYellow(final String message) {
		if (COLOR) {
			System.out.print("\033[33;1m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	synchronized public static void debug(final gateway.utils.log.LogLevel level, final String className,
			final String methodeName, final String message) {
		if (LEVEL.ordinal() >= level.ordinal()) {

			bYellow(" # [ " + level.name() + " ] ");

			yellow(className + ".");
			yellow(methodeName + "(): ");
			yellow(message + "\n");
		}
	}

	synchronized public static void error(final String className, final String methodeName, final String message) {
		if (ERROR) {
			bRed(" ! [ ERROR ] ");
			red(className + ".");
			red(methodeName + "(): ");
			red(message + "\n");
		}
	}

	synchronized public static void input(final Device device, final String message) {
		print(device + " - " + INPUT + message);
	}

	synchronized public static void output(final Device device, final String message) {
		print(device + " - " + OUTPUT + message);
	}

	synchronized public static void print(final byte[] data) {

		if (LEVEL == LogLevel.VERBOSE) {
			activeDebug("Print buffer");

			for (final byte element : data) {
				System.out.print(String.format("%02X ", element));
			}

			System.out.println("");
		}
	}

	synchronized public static void print(final String message) {
		final Date date = new Date();
		bBlue(" * [ INFO " + dateFormat.format(date) + " ] ");
		blue(message + "\n");
	}

	private static void red(final String message) {
		if (COLOR) {
			System.out.print("\033[31m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	synchronized public static void verboseDebug(final String message) {
		bYellow(" # [ " + gateway.utils.log.LogLevel.VERBOSE.name() + " ] ");
		yellow(message + "\n");
	}

	synchronized public static void verboseDebug(final String className, final String methodeName,
			final String message) {
		debug(LogLevel.VERBOSE, className, methodeName, message);
	}

	private static void yellow(final String message) {
		if (COLOR) {
			System.out.print("\033[33m" + message + "\033[0m");
		} else {
			System.out.print(message);
		}
	}

	private Log() {
	}
}