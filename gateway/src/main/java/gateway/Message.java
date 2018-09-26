package gateway;

import utils.log.Log;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Message {

	private String topic;
	private String body;

	public Message(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
	}

	public String topic() {
		return topic;
	}

	public Message setTopic(final String topic) {

		if(null == topic) {
			Log.error("Message", "setTopic", "topic is null");
		}

		this.topic = topic;

		return this;
	}

	public String body() {
		return body;
	}

	public Message setBody(final String body) {

		if (null == body) {
			Log.error("Message", "setBody", "body is null");
		}

		this.body = body;

		return this;
	}
}
