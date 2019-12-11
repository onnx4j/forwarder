package org.forwarder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Resource<T_TS> {
	
	private Map<String, T_TS> resourceMap = new HashMap<String, T_TS>();

	public T_TS get(String name) {
		return this.resourceMap.get(name);
	}
	
	public void put(String name, T_TS tensor) {
		this.resourceMap.put(name, tensor);
	}
	
	public void merge(Resource<T_TS> otherResource) {
		this.resourceMap.putAll(otherResource.get());
	}
	
	public Map<String, T_TS> get() {
		return Collections.unmodifiableMap(this.resourceMap);
	}
	
	public Set<Entry<String, T_TS>> entrySet() {
		return this.resourceMap.entrySet();
	}

}
