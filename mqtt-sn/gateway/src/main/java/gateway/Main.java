/**
 * BSD 3-Clause Licence
 *
 * Created by arnaudoglaza on 04/07/2017.
 * Updated by pierrickmarie on 28/11/2018.
 */

package gateway;

import gateway.serial.Reader;
import gateway.utils.Config;

public class Main {

	public static final int GATEWAY_ID = 1;

	public static void main(final String[] args) {

		Config.Instance.parseArgs(args);

		Reader.Instance.start();
	}
}
