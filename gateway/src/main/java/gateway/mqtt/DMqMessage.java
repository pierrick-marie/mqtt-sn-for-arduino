package gateway.mqtt;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
@Deprecated
public class DMqMessage {

	private String topic;
	private String body;
	private volatile Integer messageId = 0;

	public DMqMessage(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
	}

	public String topic() {
		return topic;
	}

	public synchronized DMqMessage setMessageId(final Integer messageId) {
		this.messageId = messageId;

		return this;
	}

	public synchronized Integer messageId() {
		return messageId;
	}

	public String body() {
		return body;
	}

	public String toString() {
		return body;
	}

}
