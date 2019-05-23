/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.utils.Config;
import gateway.utils.log.Log;
import jssc.SerialPort;
import jssc.SerialPortException;

enum XBeeSerialPort {

	Instance;

	final String SERIAL_PORT = Config.SERIAL_PORT;

	private final SerialPort serialPort;

	XBeeSerialPort() {

		Log.debug("XBeeSerialPort", "constructor", "connection to the XBee module");

		serialPort = new jssc.SerialPort(SERIAL_PORT);

		try {
			serialPort.openPort();
			serialPort.setParams(jssc.SerialPort.BAUDRATE_9600, jssc.SerialPort.DATABITS_8,
					jssc.SerialPort.STOPBITS_1, jssc.SerialPort.PARITY_NONE);

			final int mask = jssc.SerialPort.MASK_RXCHAR + jssc.SerialPort.MASK_CTS + jssc.SerialPort.MASK_DSR;
			serialPort.setEventsMask(mask);
			Log.info("Connected to the XBee module");
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "getSerial", "Can't access to the XBee module " + SERIAL_PORT);
			Log.debug("XBeeSerialPort", "getSerial", e.getMessage());
			Log.debug("Abording");
			System.exit(-1);
		}
	}

	public synchronized SerialPort serialPort() {
		return serialPort;
	}
}
