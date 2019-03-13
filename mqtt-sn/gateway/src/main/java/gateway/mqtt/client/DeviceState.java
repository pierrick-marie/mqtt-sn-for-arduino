/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.client;

public enum DeviceState {
	ASLEEP("Asleep"), LOST("Lost"), ACTIVE("Active"), AWAKE("Awake"), DISCONNECTED("Disconnected"),
	FIRSTCONNECT("FirstConnect");

	private String state;

	public String getState() {
		return this.state;
	}

	DeviceState(final String state) {
		this.state = state;
	}
}
