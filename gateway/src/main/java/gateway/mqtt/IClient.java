package gateway.mqtt;

public interface IClient {

    Boolean connect();

    Boolean subscribe(final SnTopic topic);

    Boolean publish(final SnTopic topic, final String message);

    Boolean isConnected();

    Boolean disconnect();

}
