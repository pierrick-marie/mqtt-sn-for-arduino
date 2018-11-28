package gateway.mqtt.impl;

public class Topic {

	private final Integer id;
    	private String name;
	private Boolean subscribed;

	public Topic(final Integer id, final String name) {
		this.id = id;
		this.name = name;
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

	public String name() {
	    return name;
    }

	public String toString() {
		return name().toString();
	}
}
