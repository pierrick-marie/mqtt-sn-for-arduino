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
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public enum Reader implements SerialPortEventListener {

	Instance;

	private final int NB_THREAD_PARSER = 10;
	private final XBee xbee = XBee.Instance;

	private ExecutorService executorService;

	/*
	 * TODO private byte[] cleanBuffer(final byte[] data) { int plop; int i = 0; for
	 * (; i < data.length; i++) { if (data[i] < 0) { plop = (byte) (data[i] &
	 * 0x000000FF); Log.error("SerialReader", "cleanBuffer", "" +
	 * String.format("%02X ", plop)); } } return data; }
	 */

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

				Log.debug("SerialPortReader", "serialEvent", "creating a new message executor reader");
				/*
				 * Got the bytes in the buffer -> ready to parse the message
				 */
				executorService.submit(new RawDataParser(xbee.port.readBytes(totalInputSize)));
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

		executorService = Executors.newFixedThreadPool(NB_THREAD_PARSER);
	}
}