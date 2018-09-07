package utils;

import gateway.Main;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Log {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final String INPUT = " <- ";
	private static final String OUTPUT = " -> ";

	public Log() {

	}

	public static void input(final Client client, final String message) {
		print(INPUT + client + " " + message);
	}

	public static void output(final Client client, final String message) {
		print(OUTPUT + client + " " + message);
	}

	public static void print(final String message) {
		Date date = new Date();
		bBlue( " * [ INFO " + dateFormat.format(date) + " ] ");
		blue(message + "\n");
	}

	public static void debug(final String className, final String methodeName, final String message) {
		if (Main.DEBUG) {
			bYellow(" # [ DEBUG ] ");
			yellow(className + ".");
			yellow(methodeName + "(): ");
			yellow(message + "\n");
		}
	}

	public static void error(final String className, final String methodeName, final String message) {
		if (Main.ERROR) {
			bRed(" ! [ ERROR ] ");
			red(className + ".");
			red(methodeName + "(): ");
			red(message + "\n");
		}
	}

	private static void blue(final String message) {
		System.out.print("\033[34m" + message + "\033[0m");
	}

	private static void yellow(final String message) {
		System.out.print("\033[33m" + message + "\033[0m");
	}

	private static void red(final String message) {
		System.out.print("\033[31m" + message + "\033[0m");
	}

	private static void bBlue(final String message) {
		System.out.print("\033[34;1m" + message + "\033[0m");
	}

	private static void bYellow(final String message) {
		System.out.print("\033[33;1m" + message + "\033[0m");
	}

	private static void bRed(final String message) {
		System.out.print("\033[31;1m" + message + "\033[0m");
	}
}