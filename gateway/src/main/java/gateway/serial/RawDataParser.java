package gateway.serial;

import mqtt.sn.*;
import utils.client.Client;
import utils.address.Address16;
import utils.address.Address64;
import utils.client.Clients;
import utils.log.Log;
import utils.log.LogLevel;

import java.net.URISyntaxException;

enum RawDataParser {

	Instance;

	static byte address64[] = new byte[8];
	static byte address16[] = new byte[2];
	static int payload_length;
	static int data_type;
	static byte[] payload;
	static byte[] message;

	class ThreadParser extends Thread {

		private byte[] data;

		public ThreadParser(final byte[] data) {
			this.data = data;
		}

		public void run() {
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
			} catch(Exception e) {
				Log.error("RawDataParser", "parse", "Error while reading incoming data");
				Log.debug(LogLevel.VERBOSE,"RawDataParser", "parse", e.getMessage());
			}

			// Compute the message for each case of the following switch except for SEARCHGW
			message = new byte[payload_length - 2];
			for (i = 0; i < message.length; i++) {
				message[i] = payload[2 + i];
			}

			Client client = Clients.list.search(new Address64(address64), new Address16(address16));

			switch (data_type) {
				case 0x01:
					// SEARCHGW
					new SearchGateway(client, new Integer(payload[2]));
					break;

				case 0x04:
					// CONNECT
					new Connect(client, message);
					break;

				case 0x07:
					// WILLTOPIC
					new WillTopic(client, message);
					break;

				case 0x09:
					// WILLMESSAGE
					new WillMessage(client, message);
					break;

				case 0x0A:
					// REGISTER
					new Register(client, message);
					break;

				case 0x12:
					// SUBSCRIBE
					new Subscribe(client, message);
					break;

				case 0x18:
					// DISCONNECT
					new Disconnect(client, message);
					break;

				case 0x0C:
					// PUBLISH
					new Publish(client, message);
					break;

				case 0x0D:
					// PUBACK
					new Puback(client, message);
					break;

				case 0x16:
					// PINGREQ
					new PingReq(client, message);
					break;
			}
		}
	}

	public void parse(final byte[] data)  {
		new ThreadParser(data).start();
	}
}
