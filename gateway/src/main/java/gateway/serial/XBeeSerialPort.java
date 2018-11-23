package gateway.serial;

import gateway.Main;
import gateway.utils.Config;
import jssc.SerialPort;
import jssc.SerialPortException;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
enum XBeeSerialPort {

	Instance;

	final String SERIAL_PORT = Config.SERIAL_PORT;

	final SerialPort serialPort;

	XBeeSerialPort() {

		Log.debug(LogLevel.VERBOSE, "XBeeSerialPort", "constructor", "connection to the XBee module");

		serialPort = new jssc.SerialPort(SERIAL_PORT);

		try {
			serialPort.openPort();
			serialPort.setParams(jssc.SerialPort.BAUDRATE_9600,
				jssc.SerialPort.DATABITS_8,
				jssc.SerialPort.STOPBITS_1,
				jssc.SerialPort.PARITY_NONE);

			int mask = jssc.SerialPort.MASK_RXCHAR + jssc.SerialPort.MASK_CTS + jssc.SerialPort.MASK_DSR;
			serialPort.setEventsMask(mask);
		} catch (SerialPortException e) {
			Log.error("XBeeSerialPort", "getSerial", "Impossible to get the XBee module");
			Log.debug(LogLevel.ACTIVE,"XBeeSerialPort", "getSerial", e.getMessage());
		}
	}
}
