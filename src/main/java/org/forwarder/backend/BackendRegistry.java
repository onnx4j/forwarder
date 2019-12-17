/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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