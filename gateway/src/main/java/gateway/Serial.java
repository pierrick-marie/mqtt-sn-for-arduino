package gateway;

import jssc.SerialPort;
import jssc.SerialPortException;
import utils.Log;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Serial {

	public Serial() {

	}

	public SerialPort getSerial(String port) {

		SerialPort serialPort = new SerialPort(port);

		try {
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0);
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
			serialPort.setEventsMask(mask);
			serialPort.addEventListener(new SerialPortReader());
		} catch (SerialPortException e) {
			Log.error("Serial", "getSerial", "Impossible to get the XBee module");
			Log.debug("Serial", "getSerial", e.getMessage());
		}
		return serialPort;
	}

	public static void write(SerialPort serial, byte[] add64, byte[] add16, byte[] payload) {

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
		for (int i = 0; i < 8; i++)
			res[5 + i] = add64[i];
		for (int i = 0; i < 2; i++)
			res[13 + i] = add16[i];
		res[15] = (byte) 0x0;
		res[16] = (byte) 0x1;
		for (int i = 0; i < payload.length; i++) {
			res[17 + i] = payload[i];
		}
		int cs = 0;
		for (int i = 3; i < 17 + payload.length; i++)
			cs += res[i];
		cs = cs & 0xFF;
		cs = 0xFF - cs;
		res[res.length - 1] = (byte) cs;

		try {
			if (!serial.isOpened())
				serial.openPort();
			serial.setParams(SerialPort.BAUDRATE_9600,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serial.writeBytes(res);
		} catch (SerialPortException ex) {
			ex.printStackTrace();
		}
	}

}
