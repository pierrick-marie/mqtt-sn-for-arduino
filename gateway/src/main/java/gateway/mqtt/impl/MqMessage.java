package gateway.mqtt.impl;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqMessage extends MqttMessage {

    private String topic;

    public MqMessage(final String topic, final String payload) {
        this.topic = topic;
        setPayload(payload.getBytes());
    }

    public String topic() {
        return topic;
    }

    public String toString() {
        return getPayload().toString();
    }
}
