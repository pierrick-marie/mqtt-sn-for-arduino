/**
 * BSD 3-Clause Licence
 *
 * Created by pierrickmarie on 28/11/2018.
 */

package gateway.mqtt;

import gateway.mqtt.impl.Topic;

public interface IClient {

    Boolean connect();

    Boolean subscribe(final Topic topic);

    Boolean publish(final Topic topic, final String message);

    Boolean isConnected();

    Boolean disconnect();

}
