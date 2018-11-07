package gateway.serial;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import utils.Time;
import utils.log.Log;
import utils.log.LogLevel;

import java.net.URISyntaxException;

public class SerialPortReader implements SerialPortEventListener {

	public SerialPortReader() {
		try {
			XBeeSerialPort.Instance.serialPort.addEventListener(this);
		} catch (SerialPortException e) {
			Log.error("SerialPortReader", "constructor", "Serial port exception");
			Log.debug(LogLevel.VERBOSE,"SerialPortReader", "constructor", e.getMessage());
		}
	}

	class ThreadSerialReader extends Thread {

		private byte[] buffer;

		ThreadSerialReader(final byte[] buffer) {
			this.buffer = buffer;
		}

		public void run() {
			int indexOfByte = getFirstIndexforByte((byte) 0X7E, buffer);

			if (indexOfByte == -1) {
				if (verifyData(buffer)) {
					RawDataParser.Instance.parse(buffer);
				}
				return;
			}

			int i;
			while (indexOfByte != -1) {

				byte[] temp = new byte[indexOfByte];
				byte[] newBuff = new byte[buffer.length - indexOfByte];

				for (i = 0; i < temp.length; i++) {
					temp[i] = buffer[i];
				}

				if (verifyData(temp)) {
					RawDataParser.Instance.parse(temp);
				}

				for (i = 0; i < newBuff.length; i++) {
					newBuff[i] = buffer[indexOfByte + i];
				}

				buffer = newBuff;
				indexOfByte = getFirstIndexforByte((byte) 0X7E, buffer);
				Time.sleep((long) 100, "SerialPortReader.checkDuplicate(): error while checking mesage");
			}
		}
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
		new ThreadSerialReader(buffer).start();
	}

	/**
	 * The function returns the index of @searchedByte into @data.
	 *
	 * @param searchedByte The byte to search.
	 * @param data         The date to search into the @searchedByte.
	 * @return The index of @searedByte or -1 if not found.
	 */
	private  int getFirstIndexforByte(final byte searchedByte, final byte[] data) {

		for (int i = 1; i < data.length; i++) {
			if (data[i] == searchedByte) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * The function checks if the first byte of @data is equals to 0x7E else returns false.
	 * If ok, the functions returns the result of @verifyChecksum()
	 *
	 * @param data The data to verify.
	 * @return True is the @data is OK, else false.
	 */
	private  boolean verifyData(final byte[] data) {

		if (data[0] != (byte) 0x7E) {
			return false;
		}

		return verifyChecksum(data);
	}

	/**
	 * The function verifies the checksum of the @data.
	 *
	 * @param data The data to verify the checksum.
	 * @return True if the checksum is ok, else false.
	 */
	private boolean verifyChecksum(final byte[] data) {

		int checksum = 0;

		// magic number
		for (int i = 3; i < data.length; i++) {
			checksum += (data[i] & 0xFF);
		}
		checksum = checksum & 0xFF;

		if (checksum == 0xFF) {
			return true;
		} else {
			return false;
		}
	}
}