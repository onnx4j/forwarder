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