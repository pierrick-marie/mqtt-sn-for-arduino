package gateway;

import utils.log.Log;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MqttMessage {

	private String topic;
	private String body;

	public MqttMessage(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
	}

	public String topic() {
		return topic;
	}

	public MqttMessage setTopic(final String topic) {

		if(null == topic) {
			Log.error("MqttMessage", "setTopic", "topic is null");
		}

		this.topic = topic;

		return this;
	}

	public String body() {
		return body;
	}

	public MqttMessage setBody(final String body) {

		if (null == body) {
			Log.error("MqttMessage", "setBody", "body is null");
		}

		this.body = body;

		return this;
	}
}
