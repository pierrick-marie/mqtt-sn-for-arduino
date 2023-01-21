/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package coordinator.utils.serial;

import coordinator.utils.log.Log;
import coordinator.utils.log.LogLevel;
import jssc.SerialPortException;

public class XBeeReader {

	public static String read() {

		Log.debug(LogLevel.VERBOSE, "SerialPortReader", "read", "get a message");

		try {
			final String message = XBee.Instance.serialPort().readString();
			if (null == message) {
				Log.debug(LogLevel.VERBOSE, "SerialPortReader", "read", "empty message");
				return "";
			} else {
				Log.debug(LogLevel.VERBOSE, "SerialPortReader", "read", "\n" + message);
				return message;
			}
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "read", "");
			Log.debug(LogLevel.ACTIVE, "XBeeSerialPort", "read", e.getMessage());
		}
		return "";
	}
}
