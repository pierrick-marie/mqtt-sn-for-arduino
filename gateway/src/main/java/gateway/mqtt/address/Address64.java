/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.address;

import gateway.utils.log.Log;

public class Address64 extends Address {

	public Address64(byte[] address) {
		super(address);

		if (64 < address.length) {
			Log.error("Address64", "constructor", "The address length is too long: " + address.length);
		}
	}

	/**
	 * Override the method to compare elements (used byt he hashmap of the clients).
	 *z
	 * @param compare the element to compare.
	 * @return true is the addresses are equal.
	 */
	@Override
	public boolean equals(final Object compare) {

		Address64 address = (Address64) compare;

		if (stringAddress.equals(address.stringAddress)) {
			return true;
		}

		return false;
	}

	/**
	 * Override the method to compare elements (used byt he hashmap of the clients).
	 *
	 * @return the hashcode of the string address
	 */
	@Override
	public int hashCode() {
		return stringAddress.hashCode();
	}
}
