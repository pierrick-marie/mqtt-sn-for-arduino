package gateway;

import utils.log.Log;
import utils.log.LogLevel;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MqttMessage {

	public enum MessageState {NOT_SEND, SENT, ACQUITTED, TO_DELETE};

	private final long TIME_TO_RESENT = 2000; // millisecond - 2 seconds
	private final int NB_RESENT = 2;

	private String topic;
	private String body;
	private MessageState state;

	private volatile Integer messageId = 0;
	private volatile long sentTime = 0;
	private volatile Integer nbSentTime = 0;

	public MqttMessage(final String topic, final String body) {
		this.body = body;
		this.topic = topic;
		state = MessageState.NOT_SEND;
	}

	public String topic() {
		return topic;
	}

	public MqttMessage setMessageId(final Integer messageId) {
		this.messageId = messageId;

		return this;
	}

	public Integer messageId() {
		return messageId;
	}

	public String body() {
		return body;
	}

	public synchronized Boolean needToSend() {

		Log.debug(LogLevel.ACTIVE, "MqttMessage", "needToSend", "");

		return ( (System.currentTimeMillis() >= (sentTime + TIME_TO_RESENT)) && (state.equals(MessageState.NOT_SEND)) );
	}

	public synchronized MqttMessage updateSentTime() {

		Log.debug(LogLevel.ACTIVE, "MqttMessage", "updateSentTime", "");

		sentTime = System.currentTimeMillis();
		state = MessageState.SENT;
		nbSentTime++;

		return this;
	}

	public synchronized Boolean acquitted() {

		Log.debug(LogLevel.ACTIVE, "MqttMessage", "acquitted", "");

		return state.equals(MessageState.ACQUITTED);
	}

	public synchronized Boolean needToResend() {

		Log.debug(LogLevel.ACTIVE, "MqttMessage", "needToResend", "");

		if( nbSentTime > NB_RESENT ) {
			state = MessageState.TO_DELETE;
			return false;
		} else {
			return ( (state.equals(MessageState.SENT)) && (System.currentTimeMillis() >= (sentTime + TIME_TO_RESENT)) );
		}
	}

	public synchronized Boolean needToDelete() {

		Log.debug(LogLevel.ACTIVE, "MqttMessage", "needToDelete", "");

		return ( (state.equals(MessageState.TO_DELETE)) || state.equals(MessageState.ACQUITTED) );
	}
}
