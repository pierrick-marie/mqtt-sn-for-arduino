/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package coordinator.thread;

import java.util.ConcurrentModificationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coordinator.data.Data;
import coordinator.utils.log.Log;
import coordinator.utils.log.LogLevel;
import coordinator.utils.serial.XBeeReader;

public class Receiver implements Runnable {

	private static final int SLEEP_TIME_MILLIS = 500; // 1 seconds
	private static final String PATTERN = "^\\d\\*.+$";
	private static final String SEPARATOR = "\\*";

	private final Data data;
	private final Pattern pattern = Pattern.compile(PATTERN); // [1 digit]*[text]

	private Matcher matcher;

	public Receiver(final Data data) {
		this.data = data;
	}

	@Override
	public void run() {
		String message;
		String[] dataMessage;

		while (true) {

			try {

				message = XBeeReader.read();

				for (final String m : message.split("\n")) {

					matcher = pattern.matcher(m);

					if (matcher.matches()) {

						dataMessage = m.split(SEPARATOR);

						Log.print("Get: " + dataMessage[1] + " id: " + dataMessage[0]);

						data.setData(Integer.parseInt(dataMessage[0]), dataMessage[1]);
					}
					try {
						Thread.sleep(SLEEP_TIME_MILLIS);
					} catch (final InterruptedException e) {
						Log.error("Receiver", "run", "Error during sleep");
						Log.debug(LogLevel.ACTIVE, "Receiver", "run", e.getMessage());
					}
				}

			} catch (final ConcurrentModificationException e) {
				Log.error("Receiver", "run", "Error during data access");
				Log.debug(LogLevel.ACTIVE, "Receiver", "run", e.getMessage());
			} catch (final RuntimeException e) {
				Log.error("Receiver", "run", "Error");
				Log.debug(LogLevel.ACTIVE, "Receiver", "run", e.getMessage());
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getCause().getMessage());
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getLocalizedMessage());
			}
		}
	}

	public void start() {
		new Thread(this).start();
	}
}
