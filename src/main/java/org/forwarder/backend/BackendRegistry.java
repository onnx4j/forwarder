package org.forwarder.backend;

import java.util.HashMap;
import java.util.Map;

import org.forwarder.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BackendRegistry {

	Instance;
	
	private static Logger logger = LoggerFactory.getLogger(BackendRegistry.class);

	private Map<String, Class<? extends Backend>> backends = new HashMap<String, Class<? extends Backend>>();

	public Class<? extends Backend> get(String backendName) {
		return this.backends.get(backendName);
	}

	public void register(Backend backend) {
		this.backends.put(backend.getName(), backend.getClass());
		logger.info("Backend named \"{}\" has installed", backend.getName());
	}
	
	public Map<String, Class<? extends Backend>> get() {
		return this.backends;
	}

}
