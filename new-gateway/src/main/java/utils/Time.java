package utils;

import utils.log.Log;
import utils.log.LogLevel;

public class Time {

	public  static void sleep(final Long millis, final String errorMessage) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Log.error("Time", "sleep","An exception have been captured");
			Log.debug(LogLevel.VERBOSE,"Time", "sleep", e.getMessage());
		}
	}
}
