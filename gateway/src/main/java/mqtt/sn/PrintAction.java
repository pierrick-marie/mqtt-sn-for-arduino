package mqtt.sn;

import utils.log.Log;
import utils.log.LogLevel;

public class PrintAction implements SnAction {

	public PrintAction() {
	}

	@Override
	public void exec() {
		Log.debug(LogLevel.VERBOSE, "PrintAction", "exec", "test - print message");
	}
}
