/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gateway.utils.Time;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialPortReader implements SerialPortEventListener {

	private final int NB_THREAD_PARSER = 10;

	final ExecutorService executorService;

	public SerialPortReader() {
		try {
			XBeeSerialPort.Instance.serialPort.addEventListener(this);
		} catch (final SerialPortException e) {
			Log.error("SerialPortReader", "constructor", "Serial port exception");
			Log.debug(LogLevel.VERBOSE, "SerialPortReader", "constructor", e.getMessage());
		}

		executorService = Executors.newFixedThreadPool(NB_THREAD_PARSER);
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		Log.debug(LogLevel.VERBOSE, "SerialPortReader", "serialEvent", "new event");

		int inputBufferSize;
		int totalInputSize = 0;

		if (event.isRXCHAR()) {
			try {
				inputBufferSize = XBeeSerialPort.Instance.serialPort.getInputBufferBytesCount();
				while (inputBufferSize > totalInputSize) {
					totalInputSize = inputBufferSize;
					Time.sleep((long) 100, "SerialPortReader().serialEvent: error buffering message");
					inputBufferSize = XBeeSerialPort.Instance.serialPort.getInputBufferBytesCount();
				}

				Log.debug(LogLevel.VERBOSE, "SerialPortReader", "serialEvent", "creating a new executor");
				executorService.submit(
						new ExecutorReader(XBeeSerialPort.Instance.serialPort.readBytes(totalInputSize)));
			} catch (final SerialPortException e) {
				Log.error("SerialPortReader", "serialEvent", "");
				Log.debug(LogLevel.VERBOSE, "SerialPortReader", "serialEvent", e.getMessage());
			}
		}
	}
}