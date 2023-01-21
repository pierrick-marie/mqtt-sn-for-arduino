/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package coordinator.utils.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	public static final Boolean ERROR = true;
	public static LogLevel LEVEL = LogLevel.VERBOSE;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static void bBlue(final String message) {
		System.out.print("\033[34;1m" + message + "\033[0m");
	}

	private static void blue(final String message) {
		System.out.print("\033[34m" + message + "\033[0m");
	}

	private static void bRed(final String message) {
		System.out.print("\033[31;1m" + message + "\033[0m");
	}

	private static void bYellow(final String message) {
		System.out.print("\033[33;1m" + message + "\033[0m");
	}

	public static void debug(final LogLevel level, final String className, final String methodeName,
			final String message) {
		if (LEVEL.ordinal() >= level.ordinal()) {

			bYellow(" # [ " + level.name() + " ] ");

			yellow(className + ".");
			yellow(methodeName + "(): ");
			yellow(message + "\n");
		}
	}

	public static void error(final String className, final String methodeName, final String message) {
		if (ERROR) {
			bRed(" ! [ ERROR ] ");
			red(className + ".");
			red(methodeName + "(): ");
			red(message + "\n");
		}
	}

	public static void print(final String message) {
		final Date date = new Date();
		bBlue(" * [ INFO " + dateFormat.format(date) + " ] ");
		blue(message + "\n");
	}

	private static void red(final String message) {
		System.out.print("\033[31m" + message + "\033[0m");
	}

	private static void yellow(final String message) {
		System.out.print("\033[33m" + message + "\033[0m");
	}

	public Log() {

	}
}