/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.mqtt.MessageStructure;
import gateway.mqtt.XBeeMessageType;
import gateway.mqtt.client.Device;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import jssc.SerialPortException;

public class SerialPortWriter {

	public synchronized static void write(final Device device, final byte[] payload) {

		Log.debug(LogLevel.VERBOSE, "SerialPortWriter", "write", "sending a message");

		final byte[] res = new byte[MessageStructure.TRANSMIT_LENGHT + payload.length];
		res[MessageStructure.START_DELIMITER] = XBeeMessageType.START_DELIMITER;

		if (payload.length > 255) {
			res[MessageStructure.LENGTH_START] = (byte) (res.length / 255);
			res[MessageStructure.LENGTH_START + 1] = (byte) (res.length % 255);
		} else {
			res[MessageStructure.LENGTH_START] = 0;
			res[MessageStructure.LENGTH_START + 1] = (byte) (res.length - 4);
		}

		res[MessageStructure.FRAME_TYPE] = XBeeMessageType.FRAME_TYPE_TRANSMIT_REQUEST;
		res[MessageStructure.FRAME_ID] = XBeeMessageType.FRAME_ID_WITHOUT_ACK;

		for (int i = 0; i < MessageStructure.ADDRESS_64_SIZE; i++) {
			res[MessageStructure.TRANSMIT_ADDRESS_64_START + i] = device.address64().address[i];
		}

		for (int i = 0; i < MessageStructure.ADDRESS_16_SIZE; i++) {
			res[MessageStructure.TRANSMIT_ADDRESS_16_START + i] = device.address16().address[i];
		}
		res[MessageStructure.TRANSMIT_BROADCAST] = (byte) XBeeMessageType.BROADCAST_RADIUS_ZERO;
		res[MessageStructure.TRANSMIT_OPTION] = (byte) XBeeMessageType.OPTION_DISABLE_RETRIES;

		for (int i = 0; i < payload.length; i++) {
			res[MessageStructure.TRANSMIT_PAYLOAD_START + i] = payload[i];
		}

		int checksum = 0;
		// 3 magic number
		for (int i = 3; i < MessageStructure.TRANSMIT_PAYLOAD_START + payload.length; i++) {
			checksum += res[i];
		}
		checksum = checksum & XBeeMessageType.CHECKSUM_VALUE;
		checksum = XBeeMessageType.CHECKSUM_VALUE - checksum;
		res[res.length - 1] = (byte) checksum;

		try {
			XBeeSerialPort.Instance.serialPort().writeBytes(res);
		} catch (final SerialPortException e) {
			Log.error("XBeeSerialPort", "write", "");
			Log.debug(LogLevel.ACTIVE, "XBeeSerialPort", "write", e.getMessage());
		}
	}
}
