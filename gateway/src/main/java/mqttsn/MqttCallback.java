package mqttsn;

import gateway.Main;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import utils.Client;
import utils.Log;

public class MqttCallback implements Callback<Void> {

	private final Client client;
	private final Boolean isValid;

	public MqttCallback(final Client client, final Boolean isValid) {
		this.client = client;
		this.isValid = isValid;
	}

	@Override
	public void onSuccess(Void value) {
		Log.print("Success on connect callback");
		connack();
	}

	public void onFailure(Throwable e) {
		Log.print("Failure on connect callback");
		Log.debug("MqttCallback", "onFailure", e.getMessage());
	}

	public void connack() {

		Log.print("<- " + client.name() + " Connack");

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;

		if (isValid) {
			serialMesasge[2] = (byte) 0x00;
		} else {
			serialMesasge[2] = (byte) 0x03;
		}

		Serial.write(Main.SerialPort, client.address64(), client.address16(), serialMesasge);
	}
}
