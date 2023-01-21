/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */
package gateway.serial;

import gateway.utils.Config;
import gateway.utils.log.Log;
import jssc.SerialPort;
import jssc.SerialPortException;

enum XBee {

	Instance;

	public final SerialPort port = new jssc.SerialPort(Config.Instance.serialPort());

	XBee() {

		Log.debug("XBee", "<init>", "connection to the XBee module");

		try {
			port.openPort();
			port.setParams(jssc.SerialPort.BAUDRATE_9600, jssc.SerialPort.DATABITS_8, jssc.SerialPort.STOPBITS_1,
					jssc.SerialPort.PARITY_NONE);

			final int mask = jssc.SerialPort.MASK_RXCHAR + jssc.SerialPort.MASK_CTS + jssc.SerialPort.MASK_DSR;
			port.setEventsMask(mask);
			Log.info("Gateway connected to the XBee module " + Config.Instance.serialPort());
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "getSerial",
					"Can't access to the XBee module " + Config.Instance.serialPort());
			Log.debug("XBeeSerialPort", "getSerial", e.getMessage());
			Log.debug("Abording");
			System.exit(-1);
		}
	}
}
