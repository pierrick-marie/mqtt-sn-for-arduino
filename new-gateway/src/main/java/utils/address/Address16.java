package utils.address;

import utils.Log;

public class Address16 extends Address{

	public Address16(byte[] address) {
		super(address);

		if(16 != address.length) {
			Log.error("Address16", "constructor", "The address length is too long: " + address.length);
		}
	}
}
