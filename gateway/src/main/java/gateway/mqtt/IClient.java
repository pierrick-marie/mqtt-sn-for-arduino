package gateway.mqtt;

import gateway.mqtt.client.Device;

public interface IClient {

    Boolean connect();

    Boolean subscribe(final SnTopic topic);

    Boolean publish(final SnTopic topic, final String message, final Boolean retain);

    Boolean isConnected();

    Boolean disconnect();

}
