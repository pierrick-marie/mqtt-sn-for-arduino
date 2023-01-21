/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */

package coordinator;

import coordinator.data.Data;
import coordinator.thread.Receiver;
import coordinator.thread.Sender;
import coordinator.utils.Config;
import coordinator.utils.log.Log;
import coordinator.utils.log.LogLevel;

public class Main {

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
