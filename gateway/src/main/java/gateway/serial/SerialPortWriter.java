package gateway.serial;

import jssc.SerialPortException;
import utils.client.Client;
import utils.log.Log;
import utils.log.LogLevel;

public class SerialPortWriter {

	public static void write(final Client client, final byte[] payload) {

		Log.debug(LogLevel.VERBOSE, "SerialPortWriter", "write", "sending a message");

		byte[] res = new byte[18 + payload.length];
		res[0] = 0x7E;

		if (payload.length > 255) {
			res[1] = (byte) (res.length / 255);
			res[2] = (byte) (res.length % 255);
		} else {
			res[1] = 0;
			res[2] = (byte) (res.length - 4);
		}

		res[3] = 0x10;
		res[4] = 0x01;

		for (int i = 0; i < 8; i++) {
			res[5 + i] = client.address64.address[i];
		}

		for (int i = 0; i < 2; i++) {
			res[13 + i] = client.address16.address[i];
		}
		res[15] = (byte) 0x0;
		res[16] = (byte) 0x1;

		for (int i = 0; i < payload.length; i++) {
			res[17 + i] = payload[i];
		}

		int cs = 0;

		for (int i = 3; i < 17 + payload.length; i++) {
			cs += res[i];
		}
		cs = cs & 0xFF;
		cs = 0xFF - cs;
		res[res.length - 1] = (byte) cs;

		try {
			XBeeSerialPort.Instance.serialPort.writeBytes(res);
		} catch (SerialPortException e) {
			Log.error("XBeeSerialPort", "write", "");
			Log.debug(LogLevel.ACTIVE,"XBeeSerialPort", "write", e.getMessage());
		}
	}
}
