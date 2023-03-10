/**
 * BSD 3-Clause Licence
 *
 * Created by Arnaud OGLAZA on 04/07/2017.
 * Updated by Pierrick MARIE on 28/11/2018.
 */
package gateway.utils.log;

public enum LogLevel {
	NONE("NONE"), ACTIVE("ACTIVE"), VERBOSE("VERBOSE");

	private String name;

	private LogLevel(final String value) {
		name = value;
	}

	public String getName() {
		return name;
	}
}
