/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 20/01/2023.
 */

package gateway;

import gateway.data.Data;
import gateway.thread.Receiver;
import gateway.thread.Sender;
import gateway.utils.Config;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;

public class Main {

	private int a;
	public static final int NB_XBEE_MODULE = 7;

	public static void main(String[] args) {

		Config.instance.parseArgs(args);
		Log.LEVEL = LogLevel.ACTIVE;

		final Data data = new Data();
		data.start();
		new Sender(data).start();
		new Receiver(data).start();
	}
}
