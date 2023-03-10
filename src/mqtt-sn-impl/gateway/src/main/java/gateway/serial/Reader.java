/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 *                           on 20/01/2023.
 */

package gateway.serial;

import gateway.utils.Time;
import gateway.utils.log.Log;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public enum Reader implements SerialPortEventListener {

	Instance;

	private final int NB_THREAD_PARSER = 10;
	private final XBee xbee = XBee.Instance;

	@Override
	public synchronized void serialEvent(SerialPortEvent event) {

		int inputBufferSize;
		int totalInputSize = 0;

		if (event.isRXCHAR()) {
			try {
				inputBufferSize = xbee.port.getInputBufferBytesCount();
				while (inputBufferSize > totalInputSize) {
					totalInputSize = inputBufferSize;
					Time.sleep((long) 100, "SerialPortReader().serialEvent: error buffering message");
					inputBufferSize = xbee.port.getInputBufferBytesCount();
				}

				/*
				 * Got the bytes in the buffer -> ready to parse the message
				 */
				new Thread(new RawDataParser(xbee.port.readBytes(totalInputSize))).start();
				Log.debug("SerialPortReader", "serialEvent", "new message executor reader started");
				xbee.port.purgePort(SerialPort.PURGE_RXCLEAR);
			} catch (final SerialPortException e) {
				Log.error("SerialPortReader", "serialEvent", "");
				Log.debug("SerialPortReader", "serialEvent", e.getMessage());
			}
		}
	}

	public void start() {
		try {
			xbee.port.addEventListener(this);
		} catch (final SerialPortException e) {
			Log.error("SerialPortReader", "init", "Serial port exception");
			Log.debug("SerialPortReader", "init", e.getMessage());
		}
	}
}