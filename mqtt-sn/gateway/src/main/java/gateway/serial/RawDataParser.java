/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.mqtt.MessageStructure;
import gateway.mqtt.MqttSNMessageType;
import gateway.mqtt.XBeeMessageType;
import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.mqtt.client.Device;
import gateway.mqtt.client.Devices;
import gateway.mqtt.sn.impl.Connect;
import gateway.mqtt.sn.impl.Disconnect;
import gateway.mqtt.sn.impl.PingReq;
import gateway.mqtt.sn.impl.Publish;
import gateway.mqtt.sn.impl.Register;
import gateway.mqtt.sn.impl.SearchGateway;
import gateway.mqtt.sn.impl.Subscribe;
import gateway.utils.log.Log;

public class RawDataParser implements Runnable {

	private final byte[] buffer;
	private int payload_length;
	private int data_type;
	private final byte address16[] = new byte[MessageStructure.ADDRESS_16_SIZE];
	private final byte address64[] = new byte[MessageStructure.ADDRESS_64_SIZE];

	public RawDataParser(final byte[] buffer) {
		this.buffer = buffer;
	}

	/**
	 * The function returns the index of @searchedByte into @data.
	 *
	 * @param searchedByte The byte to search.
	 * @param buffer       The date to search into the @searchedByte.
	 * @return The index of @searedByte or -1 if not found.
	 */
	/*
	 * TODO DEBUG private int getFirstIndexforByte(final byte searchedByte, final
	 * byte[] buffer) {
	 *
	 * for (int i = 1; i < buffer.length; i++) { if (buffer[i] == searchedByte) {
	 * return i; } }
	 *
	 * return -1; }
	 */

	private synchronized void parse(final byte[] buffer) {

		if (buffer[MessageStructure.FRAME_TYPE] == (byte) XBeeMessageType.TRANSMIT_STATUS) {
			Log.error("RawDataParser", "parse", "MessageStructure.FRAME_TYPE == MessageType.FRAME_TYPE_ERROR -> "
					+ String.format("%02X ", buffer[MessageStructure.FRAME_TYPE]));
			// Log.print(buffer);
			return;
		}

		// for loops
		int i;

		Log.debug("RawDataParser", "parse", "64b address");

		for (i = 0; i < MessageStructure.ADDRESS_64_SIZE; i++) {
			address64[i] = buffer[MessageStructure.RECEIVE_ADDRESS_64_START + i];
		}

		Log.debug("RawDataParser", "parse", "16b address");

		for (i = 0; i < MessageStructure.ADDRESS_16_SIZE; i++) {
			address16[i] = buffer[MessageStructure.RECEIVE_ADDRESS_16_START + i];
		}

		Log.debug("RawDataParser", "parse", "payload");

		try {
			payload_length = buffer[MessageStructure.PAYLOAD_LENGTH];
			data_type = buffer[MessageStructure.DATA_TYPE];
		} catch (final Exception e) {
			Log.error("RawDataParser", "parse", "Error while reading incoming data");
			Log.debug("RawDataParser", "parse", e.getMessage());
		}

		// Compute the message for each case of the following switch
		final byte[] data = new byte[payload_length - MessageStructure.CHECKSUM_SIZE];
		for (i = 0; i < data.length; i++) {
			data[i] = buffer[MessageStructure.RECEIVE_PAYLOAD_START + i];
		}

		final Device device = Devices.list.search(new Address64(address64), new Address16(address16));
		device.updateTimer();

		Log.debug("RawDataParser", "parse", "analyse message type");

		switch (data_type) {
		case MqttSNMessageType.SEARCHGW:
			device.setAction(new SearchGateway(device, Integer.valueOf(data[MessageStructure.RECEIVE_RADIUS])));
			break;

		case MqttSNMessageType.CONNECT:
			device.setAction(new Connect(device, data));
			break;

		case MqttSNMessageType.REGISTER:
			device.setAction(new Register(device, data));
			break;

		case MqttSNMessageType.SUBSCRIBE:
			device.setAction(new Subscribe(device, data));
			break;

		case MqttSNMessageType.DISCONNECT:
			device.setAction(new Disconnect(device, data));
			break;

		case MqttSNMessageType.PUBLISH:
			device.setAction(new Publish(device, data));
			break;

		case MqttSNMessageType.PINGREQ:
			device.setAction(new PingReq(device, data));
			break;

		case MqttSNMessageType.WILLTOPIC:
			// @TODO not implemented yet
			// device.setAction(new WillTopic(device, message));
			break;

		case MqttSNMessageType.WILLMSG:
			// @TODO not implemented yet
			// device.setAction(new WillMessage(device, message));
			break;

		case MqttSNMessageType.PUBACK:
			// PUBACK
			// Only used with QoS level 1 and 2 - not used yet
			// device.setAction(new Puback(device, message));
			break;
		}
	}

	@Override
	public synchronized void run() {

		Log.debug("RawDataParser", "run", "start the message executor reader");

		/*
		 * TODO DEBUG final int indexOfByte = getFirstIndexforByte((byte) 0X7E, buffer);
		 */
		if (verifyData(buffer)) {
			if (verifyChecksum(buffer)) {
				parse(buffer);
			}
		}
	}

	/**
	 * The function verifies the checksum of the @data.
	 *
	 * @param buffer The data to verify the checksum.
	 * @return True if the checksum is ok, else false.
	 */
	private boolean verifyChecksum(final byte[] buffer) {

		int checksum = 0;

		// 3 magic number
		for (int i = 3; i < buffer.length; i++) {
			checksum += buffer[i] & 0xFF;
		}
		checksum = checksum & 0xFF;

		if (checksum == 0xFF) {
			Log.debug("RawDataParser", "verifyChecksum", "OK");
			return true;
		} else {
			Log.error("RawDataParser", "verifyChecksum", "Fail");
			Log.debug("RawDataParser", "verifyChecksum", "checksum: " + checksum);
			return false;
		}
	}

	/**
	 * The function checks if the first byte of @data is equals to 0x7E else returns
	 * false. If ok, the functions returns the result of @verifyChecksum()
	 *
	 * @param buffer The data to verify.
	 * @return True is the @data is OK, else false.
	 */
	private boolean verifyData(final byte[] buffer) {

		if (buffer[0] != (byte) 0x7E) {
			Log.error("RawDataParser", "verifyData", "Fail");
			Log.debug("RawDataParser", "run", "start delimiter: " + buffer[0]);
			return false;
		}

		Log.debug("RawDataParser", "verifyData", "OK");
		return true;
	}
}
