/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package coordinator.utils.serial;

import coordinator.utils.log.Log;
import coordinator.utils.log.LogLevel;
import jssc.SerialPortException;

public class XBeeWriter {

	public static void write(final String message) {

		Log.debug(LogLevel.VERBOSE, "SerialPortWriter", "write", "sending a message");

		try {
			XBee.Instance.serialPort().writeString(message + "\n");
			Log.debug(LogLevel.VERBOSE, "SerialPortWriter", "write", "sent:" + message);
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "write", message);
			Log.debug(LogLevel.ACTIVE, "XBeeSerialPort", "write", e.getMessage());
		}
	}
}
