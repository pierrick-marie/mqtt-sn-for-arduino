package mqtt;

import java.util.*;

public class Topics {

	private final List<SnTopic> topics = Collections.synchronizedList(new ArrayList<>());

	synchronized public SnTopic put(final Integer id, final String name) {

		SnTopic ret = new SnTopic(id, name);
		topics.add(ret);

		return ret;
	}

	synchronized public Integer size() {
		return topics.size();
	}

	synchronized public Boolean contains(final Integer id) {

		for(SnTopic topic : topics) {
			if(topic.id().equals(id)){
				return true;
			}
		}

		return false;
	}

	synchronized public Boolean contains(final String name) {

		for(SnTopic topic : topics) {
			if(topic.name().toString().equals(name)){
				return true;
			}
		}

		return false;
	}

	synchronized public SnTopic get(final String name) {

		for(SnTopic topic : topics) {
			if(topic.name().toString().equals(name)){
				return topic;
			}
		}

		return null;
	}

	synchronized public SnTopic get(final Integer id) {

		for(SnTopic topic : topics) {
			if(topic.id().equals(id)){
				return topic;
			}
		}

		return null;
	}
}
