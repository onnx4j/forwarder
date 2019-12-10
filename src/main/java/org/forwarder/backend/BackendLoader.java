package org.forwarder.backend;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.forwarder.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BackendLoader {

	Instance;
	
	private static Logger logger = LoggerFactory.getLogger(BackendLoader.class);
	
	private boolean hasInitialized = false;

	public void initialize() {
		if (hasInitialized == false) {
			ServiceLoader<Backend> backends = ServiceLoader.load(Backend.class);
			Iterator<Backend> it = backends.iterator();
			
			if (it.hasNext() == false) {
				logger.warn("No any backends found");
			} else {
				for (; it.hasNext();) {
					Backend backend = it.next();
					assert backend != null;
					BackendRegistry.Instance.register(backend);
				}
			}
			
			hasInitialized = true;
			logger.warn("Initialization has done");
		} else {
			logger.warn("BackendLoader had initialized");
		}
	}
}
