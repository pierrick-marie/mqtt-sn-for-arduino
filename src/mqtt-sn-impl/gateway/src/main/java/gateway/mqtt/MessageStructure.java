/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt;

public class MessageStructure {

	public static final int START_DELIMITER = 0;

	public static final int LENGTH_START = 1;
	public static final int LENGTH_SIZE = 2;

	public static final int FRAME_TYPE = 3;

	public static final int FRAME_ID = 4;

	public static final int TRANSMIT_ADDRESS_64_START = 5;
	public static final int RECEIVE_ADDRESS_64_START = 4;
	public static final int ADDRESS_64_SIZE = 8;

	public static final int TRANSMIT_ADDRESS_16_START = 13;
	public static final int RECEIVE_ADDRESS_16_START = 12;
	public static final int ADDRESS_16_SIZE = 2;

	public static final int SOURCE_ENDPOINT = 14;

	public static final int TRANSMIT_BROADCAST = 15;
	public static final int TRANSMIT_OPTION = 16;

	public static final int RECEIVE_DESTINATION_ENDPOINT = 15;

	public static final int RECEIVE_CLUSTER_ID_START = 16;
	public static final int CLUSTER_ID_SIZE = 2;

	public static final int TRANSMIT_LENGHT = 18;

	public static final int PROFILE_ID_START = 18;
	public static final int PROFILE_ID_SIZE = 2;

	public static final int RECEIVE_OPTION = 20;

	public static final int PAYLOAD_LENGTH = 21; // payload[0]

	public static final int DATA_TYPE = 22; // // payload[1]

	public static final int TRANSMIT_PAYLOAD_START = 17;
	public static final int RECEIVE_PAYLOAD_START = 23; // without paylaod length and data type ->
										// payload[2]...payload[n]

	public static final int CHECKSUM_SIZE = 2;

	public static final int RECEIVE_RADIUS = 0;
}
