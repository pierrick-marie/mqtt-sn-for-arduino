package utils.address;

public abstract class Address implements Comparable<Address> {

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


	@Override
	public int compareTo(Address address) {
		if(stringAddress.equals(address.stringAddress)) {
			return 0;
		}

		return -1;
	}
}
