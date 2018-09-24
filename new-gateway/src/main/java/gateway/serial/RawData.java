package gateway.serial;

import mqttsn.*;
import utils.client.Client;
import utils.address.Address16;
import utils.address.Address64;

import java.net.URISyntaxException;

enum RawData {

	Instance;

	static byte address64[] = new byte[8];
	static byte address16[] = new byte[2];
	static int payload_length;
	static int data_type;
	static byte[] payload;
	static byte[] message;

	public void parse(byte[] data) throws URISyntaxException, InterruptedException {

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

		// Compute the message for each case of the following switch except for SEARCHGW
		message = new byte[payload_length - 2];
		for (i = 0; i < message.length; i++) {
			message[i] = payload[2 + i];
		}

		Client client = new Client(new Address64(address64), new Address16(address16));

		switch (data_type) {
			case 0x01:
				// SEARCHGW
				SearchGateway searchGW = new SearchGateway(client, new Integer(payload[2]));
				client.save();
				searchGW.start();
				break;

			case 0x04:
				// CONNECT
				Connect connect = new Connect(client, message);
				client.load();
				connect.start();
				break;

			case 0x07:
				// WILLTOPIC
				WillTopic willTopic = new WillTopic(client, message);
				client.load();
				willTopic.start();
				break;

			case 0x09:
				// WILLMESSAGE
				WillMessage willMessage = new WillMessage(client, message);
				client.load();
				willMessage.start();
				break;

			case 0x0A:
				// REGISTER
				Register register = new Register(client, message);
				client.load();
				register.start();
				break;

			case 0x12:
				// SUBSCRIBE
				Subscribe subscribe = new Subscribe(client, message);
				client.load();
				subscribe.start();
				break;

			case 0x18:
				// DISCONNECT
				Disconnect disconnect = new Disconnect(client, message);
				client.load();
				disconnect.start();
				break;

			case 0x0C:
				// PUBLISH
				Publish publish = new Publish(client, message);
				client.load();
				publish.start();
				break;

			case 0x0D:
				// PUBACK
				Puback puback = new Puback(client, message);
				client.load();
				puback.start();
				break;

			case 0x16:
				// PINGREQ
				PingReq pingreq = new PingReq(client, message);
				client.load();
				pingreq.start();
				break;
		}
	}
}
