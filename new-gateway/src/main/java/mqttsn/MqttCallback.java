package mqttsn;

import gateway.serial.SerialPortWriter;
import org.fusesource.mqtt.client.Callback;
import utils.Client;
import utils.log.Log;
import utils.log.LogLevel;

public class MqttCallback implements Callback<Void> {

	private final Client client;
	private final Boolean isValid;

	public MqttCallback(final Client client, final Boolean isValid) {
		this.client = client;
		this.isValid = isValid;
	}

	@Override
	public void onSuccess(Void value) {
		Log.debug( LogLevel.ACTIVE, "MqttCallback", "onSuccess", "Success on connect callback, value: " + value);
		connack();
	}

	@Override
	public void onFailure(Throwable e) {
		Log.debug(LogLevel.ACTIVE,"MqttCallback", "onFailure", e.getMessage());
	}

	public void connack() {

		Log.output(client, "connack");

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;

		if (isValid) {
			serialMesasge[2] = (byte) 0x00;
		} else {
			serialMesasge[2] = (byte) 0x03;
		}

		SerialPortWriter.write(client, serialMesasge);
	}
}
