package gateway.mqtt;

import gateway.mqtt.impl.Topic;

public interface IClient {

    Boolean connect();

    Boolean subscribe(final Topic topic);

    Boolean publish(final Topic topic, final String message);

    Boolean isConnected();

    Boolean disconnect();

}
