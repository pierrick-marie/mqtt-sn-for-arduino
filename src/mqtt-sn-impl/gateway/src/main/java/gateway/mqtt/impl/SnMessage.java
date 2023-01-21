/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.impl;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SnMessage extends MqttMessage {

	private final String topic;

	public SnMessage(final String topic, final String payload) {
		this.topic = topic;
		setPayload(payload.getBytes());
	}

	public String topic() {
		return topic;
	}

	@Override
	public String toString() {
		return "SnMessage topic: " + topic + " payload: " + getPayload();
	}
}
