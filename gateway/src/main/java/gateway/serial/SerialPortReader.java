package gateway.serial;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import utils.Time;
import utils.log.Log;
import utils.log.LogLevel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerialPortReader implements SerialPortEventListener {

	private final int NB_THREAD_PARSER = 10;

	final ExecutorService executorService;

	public SerialPortReader() {
		try {
			XBeeSerialPort.Instance.serialPort.addEventListener(this);
		} catch (SerialPortException e) {
			Log.error("SerialPortReader", "constructor", "Serial port exception");
			Log.debug(LogLevel.VERBOSE,"SerialPortReader", "constructor", e.getMessage());
		}

		executorService = Executors.newFixedThreadPool(NB_THREAD_PARSER);
	}

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
				checkDuplicate(XBeeSerialPort.Instance.serialPort.readBytes(totalInputSize));
			} catch (SerialPortException e) {
				Log.error("SerialPortReader", "serialEvent", "");
				Log.debug(LogLevel.VERBOSE, "SerialPortReader", "serialEvent", e.getMessage());
			}
		}
	}

	private void checkDuplicate(final byte[] buffer) {
		executorService.submit(new ExecutorReader(buffer));
	}
}