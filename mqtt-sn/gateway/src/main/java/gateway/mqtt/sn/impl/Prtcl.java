/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt.sn.impl;

public class Prtcl {

	public static final byte ACCEPTED = 0x00;
	public static final byte REJECTED = 0x03;

	public static final int DEFAULT_QOS = 0;

	public static final int PAYLOAD_LENGTH = 35;

	/**
	 * @TODO not implemented yet
	 *
	 * @param qos
	 * @return
	 *
	 * 		public static QoS getQoS(final int qos) {
	 * 
	 *         switch(qos) { case 0: return QoS.AT_MOST_ONCE; case 1: return
	 *         QoS.AT_LEAST_ONCE; case 2: return QoS.EXACTLY_ONCE; default: return
	 *         QoS.AT_MOST_ONCE; } }
	 */
}
