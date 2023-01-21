/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils;

import gateway.utils.log.Log;

public class Time {

	public static void sleep(final Long millis, final String errorMessage) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			Log.error("Time", "sleep", errorMessage);
			Log.debug("Time", "sleep", e.getMessage());
		}
	}
}
