package utils;

public enum DeviceState {
	ASLEEP("Asleep"),	LOST("Lost"), ACTIVE("Active"), AWAKE("Awake"), DISCONNECTED("Disconnected"), FIRSTCONNECT("FirstConnect");

	private String state;

	public String getState() {
		return this.state;
	}

	DeviceState(final String state) {
		this.state = state;
	}
}
