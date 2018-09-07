package utils.address;

import utils.Log;

public class Address64 extends Address {

	public Address64(byte[] address) {
		super(address);

		if(16 != address.length) {
			Log.error("Address64", "constructor", "The address length is too long: " + address.length);
		}
	}
}
