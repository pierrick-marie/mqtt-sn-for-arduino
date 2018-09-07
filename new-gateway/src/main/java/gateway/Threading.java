package gateway;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Threading {

	public static Thread thread(Runnable runnable, boolean daemon) {

		Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
		return brokerThread;
	}
}
