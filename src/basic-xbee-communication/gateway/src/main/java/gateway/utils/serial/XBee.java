/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package gateway.utils.serial;

import gateway.utils.Config;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import jssc.SerialPort;
import jssc.SerialPortException;

enum XBee {

	Instance;

	final String SERIAL_PORT = Config.SERIAL_PORT;

	private final SerialPort serialPort;

	XBee() {

		Log.debug(LogLevel.VERBOSE, "XBee", "constructor", "connection to the XBee module");

		serialPort = new jssc.SerialPort(SERIAL_PORT);

		try {
			serialPort.openPort();
			serialPort.setParams(jssc.SerialPort.BAUDRATE_9600, jssc.SerialPort.DATABITS_8,
					jssc.SerialPort.STOPBITS_1, jssc.SerialPort.PARITY_NONE);

			final int mask = jssc.SerialPort.MASK_RXCHAR + jssc.SerialPort.MASK_CTS + jssc.SerialPort.MASK_DSR;
			serialPort.setEventsMask(mask);
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "getSerial", "Impossible to get the XBee module");
			Log.debug(LogLevel.ACTIVE, "XBeeSerialPort", "getSerial", e.getMessage());
		}
	}

	public synchronized SerialPort serialPort() {
		return serialPort;
	}
}
