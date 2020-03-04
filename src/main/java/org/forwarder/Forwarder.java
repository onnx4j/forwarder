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
package org.forwarder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;

import org.forwarder.backend.BackendLoader;
import org.forwarder.backend.BackendRegistry;
import org.forwarder.opset.OperatorSetLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
public class Forwarder {

	static {
		BackendLoader.Instance.initialize();
		OperatorSetLoader.Instance.initialize();
	}

	private static Logger logger = LoggerFactory.getLogger(Forwarder.class);

	private static final Map<String, Model> modelSet = new ConcurrentHashMap<String, Model>();

	public static Model load(String onnxModelPath, Config config)
			throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.debug("Load model from {}", onnxModelPath);
		String modelId = UUID.randomUUID().toString();
		Model model = new Model(modelId, onnxModelPath, config);
		if (Forwarder.modelSet.putIfAbsent(modelId, model) == null)
			return model;
		else
			throw new RuntimeException(String.format("Model Id \"%s\" has existed in model set", modelId));
	}

	public static Map<String, Model> getModelSet() {
		return Collections.unmodifiableMap(Forwarder.modelSet);
	}

	public static Model getModel(String modelId) {
		return Forwarder.modelSet.get(modelId);
	}
	
	public static Set<String> installedBackends() {
		return BackendRegistry.Instance.get().keySet();
	}

}