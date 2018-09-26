package mqtt;

import java.util.*;

public enum Topics {

	list;

	private final HashMap<String, Integer> names = new HashMap<>();

	public Integer put(final String name, final Integer id) {
		return names.put(name, id);
	}

	public Integer size() {
		return names.size();
	}

	public Boolean contains(final Integer id) {
		return names.containsValue(id);
	}

	public Set<String> getNames() {
		return names.keySet();
	}

	public Collection<Integer> getIds() {
		return names.values();
	}

	public Boolean contains(final String name) {
		return names.containsKey(name);
	}

	public Integer get(final String name) {
		return names.get(name);
	}

	public String get(final Integer id) {

		String key = "";

		for (Map.Entry<String, Integer> entry : names.entrySet()) {
			if (Objects.equals(id, entry.getValue())) {
				key = entry.getKey();
				break;
			}
		}

		return key;
	}
}
