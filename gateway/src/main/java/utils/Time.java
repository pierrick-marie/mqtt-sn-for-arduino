package utils;

public class Time {

	public  static void sleep(final Long millis, final String errorMessage) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Log.error("Time.sleep(" + millis + ")");
			Log.error(errorMessage);
			Log.debug(e.getMessage());
		}
	}
}
