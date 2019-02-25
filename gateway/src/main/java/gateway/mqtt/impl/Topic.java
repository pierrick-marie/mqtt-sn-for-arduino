/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.impl;

public class Topic {

	private final Integer id;
	private final String name;
	private Boolean isSubscribedTopic;
	private Boolean isRegisteredTopic;

	public Topic(final Integer id, final String name) {
		this.id = id;
		this.name = name;

		isRegisteredTopic = false;
		isSubscribedTopic = false;
	}

	synchronized public Integer id() {
		return id;
	}

	synchronized public Boolean isRegistered() {
		return isRegisteredTopic;
	}

	synchronized public Boolean isSubscribed() {
		return isSubscribedTopic;
	}

	public String name() {
		return name;
	}

	synchronized public Topic setRegistered(final Boolean value) {
		isRegisteredTopic = value;
		isSubscribedTopic = !value;

		return this;
	}

	synchronized public Topic setSubscribed(final Boolean value) {
		isRegisteredTopic = !value;
		isSubscribedTopic = value;

		return this;
	}

	@Override
	public String toString() {
		return name().toString();
	}
}
