package utils;

public enum State {
	ASLEEP("Asleep"),	LOST("Lost"), ACTIVE("Active"), AWAKE("Awake"), DISCONNECTED("Disconnected"), FIRSTCONNECT("FirstConnect");

	private String state;

	public String getState() {
		return this.state;
	}

	State(final String state) {
		this.state = state;
	}
}
