/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package coordinator.data;

import java.util.Date;

import coordinator.utils.log.Log;
import coordinator.utils.log.LogLevel;

class Message implements Comparable<Date> {

	private String message;
	private final Date date;

	public Message() {
		message = "";
		date = new Date();
	}

	public Message(final String message) {
		this.message = message;
		date = new Date();
	}

	public synchronized void clear() {
		message = "";
	}

	@Override
	public synchronized int compareTo(final Date comparable) {
		try {
			return (int) (date.getTime() - comparable.getTime());
		} catch (final RuntimeException e) {
			Log.error("Message", "compareTo", "Error");
			Log.debug(LogLevel.ACTIVE, "Receiver", "run", e.getMessage());
			Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getCause().getMessage());
			Log.debug(LogLevel.ACTIVE, "Sender", "run", e.getLocalizedMessage());
		}

		return 0;
	}

	public long date() {
		return date.getTime();
	}

	public synchronized Boolean isClear() {
		return message.isEmpty();
	}

	@Override
	public synchronized String toString() {
		return message;
	}
}
