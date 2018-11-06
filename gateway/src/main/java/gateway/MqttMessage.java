package gateway;

import utils.log.Log;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MqttMessage {

	private String topic;
	private String body;

	private volatile Integer messageId;
	private volatile Boolean acquitted;

	public MqttMessage(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
		acquitted = false;
	}

	public String topic() {
		return topic;
	}

	public MqttMessage setAcquitted(final Boolean acquitted) {
		this.acquitted = acquitted;

		return this;
	}

	public MqttMessage setMessageId(final Integer messageId) {
		this.messageId = messageId;

		return this;
	}

	public Boolean acquitted() {
		return acquitted;
	}

	public Integer messageId() {
		return messageId;
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
