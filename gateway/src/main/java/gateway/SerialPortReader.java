package gateway;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.net.URISyntaxException;

public class SerialPortReader implements SerialPortEventListener {

	public void serialEvent(SerialPortEvent event) {
		int inputBufferSize;
		int totalInputSize = 0;
		if (event.isRXCHAR()) {
			try {
				inputBufferSize = Main.SerialPort.getInputBufferBytesCount();
				while (inputBufferSize > totalInputSize) {
					totalInputSize = inputBufferSize;
					Thread.sleep(100);
					inputBufferSize = Main.SerialPort.getInputBufferBytesCount();
				}
				checkDuplicate(Main.SerialPort.readBytes(totalInputSize));
			} catch (SerialPortException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public  void checkDuplicate(byte[] buffer) throws URISyntaxException, InterruptedException {

		int indexOfByte = getFirstIndexforByte((byte) 0X7E, buffer);

		if (indexOfByte == -1) {
			if (verifyData(buffer)) {
				RawData.parse(buffer);
			}
			return;
		}

		// for loop
		int i;

		while (indexOfByte != -1) {

			byte[] temp = new byte[indexOfByte];
			byte[] newBuff = new byte[buffer.length - indexOfByte];

			for (i = 0; i < temp.length; i++) {
				temp[i] = buffer[i];
			}

			if (verifyData(temp)) {
				RawData.parse(temp);
			}

			for (i = 0; i < newBuff.length; i++) {
				newBuff[i] = buffer[indexOfByte + i];
			}

			buffer = newBuff;
			indexOfByte = getFirstIndexforByte((byte) 0X7E, buffer);
			Thread.sleep(100);
		}
	}

	/**
	 * The function returns the index of @searchedByte into @data.
	 *
	 * @param searchedByte The byte to search.
	 * @param data         The date to search into the @searchedByte.
	 * @return The index of @searedByte or -1 if not found.
	 */
	public int getFirstIndexforByte(byte searchedByte, byte[] data) {

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
	public  boolean verifyData(byte[] data) {

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
	public boolean verifyChecksum(byte[] data) {

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