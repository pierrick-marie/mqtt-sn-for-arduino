/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

package gateway.mqtt;

public class XBeeMessageType {

	public static final int START_DELIMITER = 0x7E;
	public static final int FRAME_TYPE_TRANSMIT_REQUEST = 0x10;
	public static final int FRAME_ID_WITHOUT_ACK = 0x00;
	public static final int BROADCAST_RADIUS_ZERO = 0x00;
	public static final int OPTION_DISABLE_RETRIES = 0x01;

	public static final int CHECKSUM_VALUE = 0xFF;

	public static final int TRANSMIT_STATUS = 0x8B;

}
