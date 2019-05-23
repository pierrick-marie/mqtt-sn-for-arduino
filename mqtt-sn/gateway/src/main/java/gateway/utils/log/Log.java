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
	public static final gateway.utils.log.LogLevel LEVEL = gateway.utils.log.LogLevel.ACTIVE;

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final String INPUT = " --->> ";
	private static final String OUTPUT = " <<--- ";

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

	synchronized private static void debug(final gateway.utils.log.LogLevel level, final String className,
			final String methodeName, final String message) {
		if (LEVEL.ordinal() >= level.ordinal()) {

			final Date date = new Date();
			bYellow(" # [ DEBUG " + dateFormat.format(date) + " ] ");

			yellow(className + ".");
			yellow(methodeName + ": ");
			yellow(message + "\n");
		}
	}

	synchronized public static void debug(final String message) {
		final Date date = new Date();
		bYellow(" # [ DEBUG " + dateFormat.format(date) + " ] ");
		yellow(message + "\n");
	}

	synchronized public static void debug(final String className, final String methodeName, final String message) {
		debug(LogLevel.ACTIVE, className, methodeName, message);
	}

	synchronized public static void error(final String className, final String methodeName, final String message) {

		final Date date = new Date();
		bRed(" # [ ERROR " + dateFormat.format(date) + " ] ");
		red(className + ".");
		red(methodeName + ": ");
		red(message + "\n");
	}

	synchronized public static void info(final String message) {
		bBlue(" * [ INFO ] ");
		blue(message + "\n");
	}

	synchronized public static void input(final Device device, final String message) {
		info(INPUT + device + ": " + message);
	}

	synchronized public static void output(final Device device, final String message) {
		info(OUTPUT + device + ": " + message);
	}

	synchronized public static void print(final byte[] data) {

		if (LEVEL == LogLevel.ACTIVE) {
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