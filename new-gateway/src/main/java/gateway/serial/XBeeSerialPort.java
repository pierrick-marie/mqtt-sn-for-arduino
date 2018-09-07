package gateway.serial;

import gateway.Main;
import jssc.SerialPort;
import jssc.SerialPortException;
import utils.Client;
import utils.Log;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
enum XBeeSerialPort {

	Instance;

	final SerialPort serialPort;

	XBeeSerialPort() {

		serialPort = new jssc.SerialPort(Main.SERIAL_PORT);

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
			Log.debug("XBeeSerialPort", "getSerial", e.getMessage());
		}
	}
}