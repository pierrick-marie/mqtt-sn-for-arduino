/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.utils.Time;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class ExecutorReader implements Runnable {

	private byte[] buffer;

	ExecutorReader(final byte[] buffer) {
		Log.debug(LogLevel.VERBOSE, "ExecutorReader", "constructor", "");
		Log.print(buffer);
		this.buffer = buffer;
	}

	/**
	 * The function returns the index of @searchedByte into @data.
	 *
	 * @param searchedByte The byte to search.
	 * @param data         The date to search into the @searchedByte.
	 * @return The index of @searedByte or -1 if not found.
	 */
	private int getFirstIndexforByte(final byte searchedByte, final byte[] data) {

		for (int i = 1; i < data.length; i++) {
			if (data[i] == searchedByte) {
				return i;
			}
		}

		return -1;
	}

	@Override
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

			final byte[] temp = new byte[indexOfByte];
			final byte[] newBuff = new byte[buffer.length - indexOfByte];

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
			checksum += data[i] & 0xFF;
		}
		checksum = checksum & 0xFF;

		if (checksum == 0xFF) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * The function checks if the first byte of @data is equals to 0x7E else returns
	 * false. If ok, the functions returns the result of @verifyChecksum()
	 *
	 * @param data The data to verify.
	 * @return True is the @data is OK, else false.
	 */
	private boolean verifyData(final byte[] data) {

		if (data[0] != (byte) 0x7E) {
			return false;
		}

		return verifyChecksum(data);
	}
}
