/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt.address;

public abstract class Address {

	public final byte address[];
	public final String stringAddress;

	public Address(final byte address[]) {
		this.address = address;
		stringAddress = addressToString(address);
	}

	public String getStringAddress() {

		return addressToString(address);
	}

	public static String addressToString(final byte[] address) {

		String ret = "";

		for (int i = 0; i < address.length; i++) {
			ret += address[i];
		}

		return ret;
	}

	public String toString() {
		return stringAddress;
	}
}
