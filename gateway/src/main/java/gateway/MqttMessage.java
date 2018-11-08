package gateway;

import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MqttMessage {

	private String topic;
	private String body;
	private volatile Integer messageId = 0;

	public MqttMessage(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
	}

	public String topic() {
		return topic;
	}

	public synchronized MqttMessage setMessageId(final Integer messageId) {
		this.messageId = messageId;

		return this;
	}

	public synchronized Integer messageId() {
		return messageId;
	}

	public String body() {
		return body;
	}

}