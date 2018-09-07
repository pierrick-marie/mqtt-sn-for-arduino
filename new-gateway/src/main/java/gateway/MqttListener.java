package gateway;

import utils.Client;
import utils.Log;
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

	private Client client;

	public MqttListener(final Client client) {
		this.client = client;
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

		Log.debug("MqttListener", "onPublish", client + " Buffering Message");

		Message msg = new Message(topic.utf8().toString(), body.utf8().toString());
		client.messages.add(msg);
		ack.run();

	}

	@Override
	public void onFailure(Throwable value) {
		Log.error("MqttListener", "onFailure", "Listener onFailure");
		Log.error("MqttListener", "onFailure", value.getMessage());
	}
}
