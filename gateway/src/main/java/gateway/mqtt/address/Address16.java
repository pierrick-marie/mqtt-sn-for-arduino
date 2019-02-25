/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.address;

import gateway.utils.log.Log;

public class Address16 extends Address {

	public Address16(byte[] address) {
		super(address);

		if (16 < address.length) {
			Log.error("Address16", "constructor", "The address length is too long: " + address.length);
		}
	}
}
