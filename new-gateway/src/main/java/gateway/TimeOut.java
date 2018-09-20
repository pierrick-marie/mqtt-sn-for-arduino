package gateway;

import utils.*;
import org.fusesource.mqtt.client.Callback;
import utils.log.Log;
import utils.log.LogLevel;

import java.beans.ExceptionListener;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class TimeOut implements Runnable, ExceptionListener {

	private final Integer duration;
	private final Client client;

	public TimeOut(final Client client, final Integer duration) {
		this.client = client;
		this.duration = duration;
	}

	public void start() {

		for (int i = 0; i < duration; i++) {

			if( client.state().equals(State.ASLEEP)) {
				Log.debug(LogLevel.ACTIVE,"TimeOut", "start", "Not asleep anymore");
			}

			Time.sleep((long)1000, "TimeOut.start()");
		}
		clientTimeOut();
	}

	private void clientTimeOut() {

		client.setState(State.LOST);

		client.connection().disconnect(new Callback<Void>() {
			@Override
			public void onSuccess(Void value) {
				// @TODO
				Log.debug(LogLevel.ACTIVE,"TimeOut", "clientTimeOut", "Success");
			}

			@Override
			public void onFailure(Throwable value) {
				Log.debug(LogLevel.VERBOSE,"TimeOut", "clientTimeOut", "Failure");
				Log.error("TimeOut", "clientTimeOut", value.getMessage());
			}
		});
	}

	@Override
	public void run() {
		this.start();
	}

	@Override
	public void exceptionThrown(Exception e) {
		e.printStackTrace();
	}
}
