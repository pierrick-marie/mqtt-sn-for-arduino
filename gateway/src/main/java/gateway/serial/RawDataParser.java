/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.mqtt.address.Address16;
import gateway.mqtt.address.Address64;
import gateway.mqtt.client.Device;
import gateway.mqtt.client.Devices;
import gateway.mqtt.sn.impl.*;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

enum RawDataParser {

	Instance;

	static byte address64[] = new byte[8];
	static byte address16[] = new byte[2];
	static int payload_length;
	static int data_type;
	static byte[] payload;
	static byte[] message;

	public void parse(final byte[] data) {

		if (data[3] == (byte) 0x8B) {
			return;
		}

		// for loop
		int i;

		// Read the first 8 bytes
		for (i = 0; i < 8; i++) {
			address64[i] = data[4 + i];
		}

		// Read the first 2 bytes
		for (i = 0; i < 2; i++) {
			address16[i] = data[12 + i];
		}

		try {
			// check the type of message
			if (data[15] == 0x01) {
				payload_length = (data[16] * 16) + data[17];
				data_type = data[18];
				payload = new byte[payload_length];
				for (i = 19; i < data.length; i++) {
					payload[i] = data[i];
				}
			} else {
				payload_length = data[15];
				data_type = data[16];
				payload = new byte[payload_length];
				for (i = 0; i < payload_length; i++) {
					payload[i] = data[15 + i];
				}
			}
		} catch (Exception e) {
			Log.error("RawDataParser", "parse", "Error while reading incoming data");
			Log.debug(LogLevel.VERBOSE, "RawDataParser", "parse", e.getMessage());
		}

		// Compute the message for each case of the following switch except for SEARCHGW
		message = new byte[payload_length - 2];
		for (i = 0; i < message.length; i++) {
			message[i] = payload[2 + i];
		}

		Device device = Devices.list.search(new Address64(address64), new Address16(address16));

		switch (data_type) {
			case 0x01:
				// SEARCHGW
				device.setAction(new SearchGateway(device, Integer.valueOf(payload[2]) ));
				break;

			case 0x04:
				// CONNECT
				device.setAction(new Connect(device, message));
				break;

			case 0x0A:
				// REGISTER
				device.setAction(new Register(device, message));
				break;

			case 0x12:
				// SUBSCRIBE
				device.setAction(new Subscribe(device, message));
				break;

			case 0x18:
				// DISCONNECT
				device.setAction(new Disconnect(device, message));
				break;

			case 0x0C:
				// PUBLISH
				device.setAction(new Publish(device, message));
				break;

			case 0x16:
				// PINGREQ
				device.setAction(new PingReq(device, message));
				break;

			case 0x07:
				// WILLTOPIC
				// @TODO not implemented yet
				// device.setAction(new WillTopic(device, message));
				break;

			case 0x09:
				// WILLMESSAGE
				// @TODO not implemented yet
				// device.setAction(new WillMessage(device, message));
				break;

			case 0x0D:
				// PUBACK
				// Only used with QoS level 1 and 2 - not used yet
				// device.setAction(new Puback(device, message));
				break;
		}
	}
}
