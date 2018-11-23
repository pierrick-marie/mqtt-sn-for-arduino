package gateway.mqtt;

import gateway.mqtt.sn.Prtcl;
import org.fusesource.mqtt.client.Topic;

public class SnTopic extends Topic {

	private final Integer id;

	private Boolean subscribed;

	public SnTopic(final Integer id, final String name) {
		super(name, Prtcl.DEFAUlT_QOS);
		this.id = id;
		subscribed = false;
	}

	synchronized public Integer id() {
		return id;
	}

	synchronized public Boolean isSubscribed() {
		return subscribed;
	}

	synchronized public Topic setSubscribed() {
		subscribed = true;

		return this;
	}

	public String toString() {
		return "topic: " + name() + " - id: " + id;
	}
}
