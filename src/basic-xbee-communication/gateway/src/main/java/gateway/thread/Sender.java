/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.thread;

import java.util.ConcurrentModificationException;

import gateway.data.Data;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import gateway.utils.serial.XBeeWriter;

public class Sender implements Runnable {

	private static final int SLEEP_TIME_MILLIS = 1000; // 1 seconds

	private final Data data;

	public Sender(final Data data) {
		this.data = data;
	}

	@Override
	public void run() {
		while (true) {
			try {
				for (final String mData : data.getData()) {
					if (!mData.isEmpty()) {
						Log.print("Send: " + mData);
						XBeeWriter.write(mData);
					}
					try {
						Thread.sleep(SLEEP_TIME_MILLIS);
					} catch (final InterruptedException e) {
						Log.debug(LogLevel.ACTIVE, "Main", "sleep", e.getMessage());
					}
				}

				try {
					Thread.sleep(10 * SLEEP_TIME_MILLIS); // 10 * 1 seconds -> 10
				} catch (final InterruptedException e) {
					Log.error("Sender", "run", "Error during sleep");
					Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getMessage());
				}
			} catch (final ConcurrentModificationException e) {
				Log.error("Sender", "run", "Error during data access");
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getMessage());
			} catch (final RuntimeException e) {
				Log.error("Sender", "run", "Error");
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getMessage());
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getCause().getMessage());
				Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getLocalizedMessage());
			}
		}
	}

	public void start() {
		new Thread(this).start();
	}

}
