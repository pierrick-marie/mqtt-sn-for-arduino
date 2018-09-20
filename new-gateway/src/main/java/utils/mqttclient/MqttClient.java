package utils.mqttclient;

import org.fusesource.mqtt.client.MQTT;
import utils.log.Log;

public class MqttClient extends MQTT {

	@Override
	public MqttConnection blockingConnection() {
		Log.acticeDebug("MqttClient - blockingConnection");
		return new MqttConnection(this.futureConnection());
	}
}
