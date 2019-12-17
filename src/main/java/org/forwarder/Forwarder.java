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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.forwarder.backend.BackendFactory;
import org.forwarder.backend.BackendLoader;
import org.forwarder.executor.Executor;
import org.forwarder.executor.ExecutorFactory;
import org.forwarder.opset.OperatorSetLoader;
import org.onnx4j.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Forwarder implements AutoCloseable {

	static {
		BackendLoader.Instance.initialize();
		OperatorSetLoader.Instance.initialize();
	}

	private static Logger logger = LoggerFactory.getLogger(Forwarder.class);

	private Config config;
	private Model model;
	private Executor<?> executor;
	private Map<String, Backend<?>> backends = new HashMap<String, Backend<?>>();

	private Forwarder(Config config) {
		this.config = config;
	}

	public static Forwarder config(Config config) {
		return new Forwarder(config);
	}

	public Forwarder load(String onnxModelPath) throws FileNotFoundException, IOException {
		this.model = new Model(onnxModelPath, this.config.getTensorOptions());
		return this;
	}

	public Backend<?> backend(String name) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (this.backends.containsKey(name)) {
			return this.backends.get(name);
		} else {
			Backend<?> backend = BackendFactory.createInstance(name, this.model, this.executor);
			assert backend != null;
			this.backends.put(name, backend);
			return backend;
		}
	}

	public Forwarder executor(Class<? extends Executor> classOfExecutor)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		this.executor = ExecutorFactory.createInstance(model, classOfExecutor);
		return this;
	}

	public Executor<?> getExecutor() {
		return this.executor;
	}

	public Config getConfig() {
		return this.config;
	}

	public Model getModel() {
		return this.model;
	}

	@Override
	public void close() throws Exception {
		for (Entry<String, Backend<?>> backend : backends.entrySet()) {
			backend.getValue().close();

			logger.debug("Backend named {} has been closed", backend.getKey());
		}

		this.model.close();
	}

}