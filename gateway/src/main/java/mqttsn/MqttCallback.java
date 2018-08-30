package mqttsn;

import gateway.Main;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import utils.Log;

public class MqttCallback implements Callback<Void> {

	private final byte[] address64;
	private final byte[] address16;
	private final Boolean isValid;

	public MqttCallback(byte[] address64, byte[] address16, final Boolean isValid) {
		this.address16 = address16;
		this.address64 = address64;
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

		Log.print("<- " + Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " Connack");

		byte[] serialMesasge = new byte[3];
		serialMesasge[0] = (byte) 0x03;
		serialMesasge[1] = (byte) 0x05;

		if (isValid) {
			serialMesasge[2] = (byte) 0x00;
		} else {
			serialMesasge[2] = (byte) 0x03;
		}

		Serial.write(Main.SerialPort, address64, address16, serialMesasge);
	}
}
