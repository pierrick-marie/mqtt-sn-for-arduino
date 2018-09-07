package gateway;

import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MultipleSender extends Thread {

	byte[] add64;
	byte[] add16;
	ArrayList<Message> messageList;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public MultipleSender(byte[] add64, byte[] add16, ArrayList<Message> messageList) {
		this.add64 = add64;
		this.add16 = add16;
		this.messageList = messageList;
	}

	public void run() {

		// DEBUG
		System.out.println("Begin Multiple Sender: " + messageList);

		for (int i = 0; i < messageList.size(); i++) {

			// DEBUG
			System.out.println("Starting to send message n°" + i);

			Sender sender = new Sender(add64, add16, messageList.get(i));

			// DEBUG
			System.out.println("Before " + i + " sender");

			sender.start();
			try {
				sender.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// DEBUG
			System.out.println("After " + i + " sender");
		}

		for (int i = 0; i < messageList.size(); i++) {
            	// @TODO DEBUG
			// Main.ClientBufferedMessage.put(Utils.byteArrayToString(add64),new ArrayList<>());
		}

		// DEBUG
		System.out.println("End of Msender");

		pingresp(add64, add16);
	}

	public void pingresp(byte[] add64, byte[] add16) {
		Date date = new Date();
		// @TODO DEBUG
		// System.out.println(sdf.format(date) + ": <- " + Main.AddressClientMap.get(Utils.byteArrayToString(add64)) + " Pingresp");
		byte[] ret = new byte[2];
		ret[0] = (byte) 0x02;
		ret[1] = (byte) 0x17;
		Serial.write(Main.SerialPort, add64, add16, ret);
		// @TODO DEBUG
		/*
		if (Main.ClientState.get(Utils.byteArrayToString(add64)).equals(utils.State.AWAKE)) {
			Main.ClientState.put(Utils.byteArrayToString(add64), utils.State.ASLEEP);
			//System.out.println(Main.AddressClientMap.get(Utils.byteArrayToString(address64))+" goes to sleep");
			if (Main.ClientDuration.get(Utils.byteArrayToString(add64)) != null) {
				TimeOut timeOut = new TimeOut(Main.ClientDuration.get(Utils.byteArrayToString(add64)), add64);
				Threading.thread(timeOut, false);
			}
		}
		*/
	}

}
