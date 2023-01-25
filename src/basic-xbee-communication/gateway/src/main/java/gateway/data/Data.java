/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 *                           on 20/01/2023.
 */

package gateway.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gateway.Main;
import gateway.utils.Config;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Data implements Runnable {

	public static final int SLEEPING_TIME_MILLIS = 1000 * 30; // 30 seconds
	public static final int EXPIRATION_TIME_MILLIS = 1000 * 60 * 3; // 3 minutes

	private final Map<Integer, Message> data = new ConcurrentHashMap<>();

	public Data() {
		for (int i = 0; i <= Config.NB_XBEE_MODULE; i++) {
			data.put(i, new Message());
		}
	}

	public synchronized Collection<String> getData() {

		final List<String> values = new ArrayList<>();

		for (final Message m : data.values()) {
			values.add(m.toString());
		}

		return values;
	}

	@Override
	public void run() {

		Date date;

		while (true) {

			try {
				Thread.sleep(SLEEPING_TIME_MILLIS);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			date = new Date();
			for (final Message m : data.values()) {
				if (EXPIRATION_TIME_MILLIS < Math.abs(m.compareTo(date)) && !m.isClear()) {
					Log.print("Delete: " + m);
					m.clear();
				}
			}
		}
	}

	public synchronized void setData(final Integer xbeeId, final String message) {

		if (0 < xbeeId && Config.NB_XBEE_MODULE >= xbeeId) {
			data.replace(xbeeId, new Message(message));
			Log.debug(LogLevel.VERBOSE, "Data", "replace", "id: " + xbeeId + " data: " + data.get(xbeeId));
		}
	}

	public void start() {
		new Thread(this).start();
	}

	@Override
	public synchronized String toString() {

		final StringBuffer buffer = new StringBuffer();

		data.forEach((key, value) -> {
			if (!value.equals("")) {
				buffer.append(value);
				buffer.append("\n");
			}
		});

		Log.debug(LogLevel.VERBOSE, "Data", "toString", buffer.toString());
		return buffer.toString();
	}
}
