package gateway;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Message {

    String topic;
    String body;

    public Message(String topic, String body){
        this.body=body;
        this.topic=topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
