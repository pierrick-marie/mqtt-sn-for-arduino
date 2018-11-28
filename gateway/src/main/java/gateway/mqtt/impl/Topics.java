package gateway.mqtt.impl;

import java.util.*;

public class Topics {

	private final List<Topic> topics = Collections.synchronizedList(new ArrayList<>());

	synchronized public Topic put(final Integer id, final String name) {

		Topic ret = new Topic(id, name);
		topics.add(ret);

		return ret;
	}

	synchronized public Integer size() {
		return topics.size();
	}

	synchronized public Boolean contains(final Integer id) {

		for(Topic topic : topics) {
			if(topic.id().equals(id)){
				return true;
			}
		}

		return false;
	}

	synchronized public Boolean contains(final String name) {

		for(Topic topic : topics) {
			if(topic.name().toString().equals(name)){
				return true;
			}
		}

		return false;
	}

	synchronized public Topic get(final String name) {

		for(Topic topic : topics) {
			if(topic.name().toString().equals(name)){
				return topic;
			}
		}

		return null;
	}

	synchronized public Topic get(final Integer id) {

		for(Topic topic : topics) {
			if(topic.id().equals(id)){
				return topic;
			}
		}

		return null;
	}
}
