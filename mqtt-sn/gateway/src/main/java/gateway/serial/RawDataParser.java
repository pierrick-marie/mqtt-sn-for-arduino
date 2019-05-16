/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway.serial;

import gateway.mqtt.MessageStructure;
import gateway.mqtt.MessageType;
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
import gateway.utils.log.LogLevel;

enum RawDataParser {

	Instance;

	private byte[] message;
	private int payload_length;
	private int data_type;
	private final byte address16[] = new byte[MessageStructure.ADDRESS_16_SIZE];
	private final byte address64[] = new byte[MessageStructure.ADDRESS_64_SIZE];

	public void parse(final byte[] data) {

		if (data[MessageStructure.FRAME_TYPE] == (byte) MessageType.FRAME_TYPE_ERROR) {
			return;
		}

		// for loops
		int i;

		for (i = 0; i < MessageStructure.ADDRESS_64_SIZE; i++) {
			address64[i] = data[MessageStructure.ADDRESS_64_START + i];
		}

		for (i = 0; i < MessageStructure.ADDRESS_16_SIZE; i++) {
			address16[i] = data[MessageStructure.ADDRESS_16_START + i];
		}

		try {
			// TODO BEGIN DEBUG 07/05/2019 14:30
			//
			// check the type of message
			// if (data[15] == 0x01) {
			// payload_length = (byte) (data[16] * 16 + data[17]);
			// data_type = data[18];
			// payload = new byte[payload_length];
			// for (i = 19; i < data.length; i++) {
			// payload[i] = data[i];
			// }
			// } else {
			// payload_length = data[15] & 0xFF;
			// data_type = data[16] & 0xFF;
			// payload = new byte[payload_length];
			// for (i = 0; i < payload_length; i++) {
			// payload[i] = data[15 + i];
			// }
			// }
			//
			// END DEBUG

			payload_length = data[MessageStructure.PAYLOAD_LENGTH];
			data_type = data[MessageStructure.DATA_TYPE];

		} catch (final Exception e) {
			Log.error("RawDataParser", "parse", "Error while reading incoming data");
			Log.debug(LogLevel.VERBOSE, "RawDataParser", "parse", e.getMessage());
		}

		// Compute the message for each case of the following switch
		message = new byte[payload_length - MessageStructure.CHECKSUM_SIZE];
		for (i = 0; i < message.length; i++) {
			message[i] = data[MessageStructure.PAYLOAD_START + i];
		}

		final Device device = Devices.list.search(new Address64(address64), new Address16(address16));
		device.updateTimer();

		switch (data_type) {
		case MessageType.SEARCHGW:
			device.resetAction();
			Log.print("JHDKJFHDJFH");
			while (device.isAlive() && !device.isInterrupted()) {
				Log.print("?????????");
				device.interrupt();
			}
			Log.print("JHDKJFHDJFHqsdqsdqsd");
			Log.print("JHDKJFHDJFHqsdqsdqsdqsdqsdqsd");
			Log.debug(LogLevel.VERBOSE, "RawDataParser", "parse", "Device - start & stop");
			device.setAction(new SearchGateway(device, Integer.valueOf(message[MessageStructure.RADIUS])));
			device.start();
			break;

		case MessageType.CONNECT:
			device.setAction(new Connect(device, message));
			break;

		case MessageType.REGISTER:
			device.setAction(new Register(device, message));
			break;

		case MessageType.SUBSCRIBE:
			device.setAction(new Subscribe(device, message));
			break;

		case MessageType.DISCONNECT:
			device.setAction(new Disconnect(device, message));
			break;

		case MessageType.PUBLISH:
			device.setAction(new Publish(device, message));
			break;

		case MessageType.PINGREQ:
			device.setAction(new PingReq(device, message));
			break;

		case MessageType.WILLTOPIC:
			// @TODO not implemented yet
			// device.setAction(new WillTopic(device, message));
			break;

		case MessageType.WILLMSG:
			// @TODO not implemented yet
			// device.setAction(new WillMessage(device, message));
			break;

		case MessageType.PUBACK:
			// PUBACK
			// Only used with QoS level 1 and 2 - not used yet
			// device.setAction(new Puback(device, message));
			break;
		}
	}
}
