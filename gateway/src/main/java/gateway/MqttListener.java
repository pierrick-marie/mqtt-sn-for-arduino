package gateway;

import utils.Utils;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class MqttListener implements Listener {

	byte[] add64;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public MqttListener(byte[] add64) {
		this.add64 = add64;
	}

	@Override
	public void onConnected() {
	}

	@Override
	public void onDisconnected() {
		//System.out.println("onDisconnected");
	}

	@Override
	public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {

		// @TODO DEBUG
		// System.out.println(Main.AddressClientMap.get(Utils.byteArrayToString(add64)) + " Buffering Message");
		Message msg = new Message(topic.utf8().toString(), body.utf8().toString());

		// @TODO DEBUG
		// ArrayList<Message> temp = Main.ClientBufferedMessage.get(Utils.byteArrayToString(add64));
		// temp.add(msg);
		// Main.ClientBufferedMessage.put(Utils.byteArrayToString(add64), temp);
		ack.run();

	}

	@Override
	public void onFailure(Throwable value) {
		Date date = new Date();
		System.err.println(sdf.format(date) + ": Listener onFailure");
		value.printStackTrace();

	}
}
