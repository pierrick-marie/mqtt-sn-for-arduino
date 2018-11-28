/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.utils;

import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Time {

	public  static void sleep(final Long millis, final String errorMessage) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Log.error("Time", "sleep",errorMessage);
			Log.debug(LogLevel.VERBOSE,"Time", "sleep", e.getMessage());
		}
	}
}
