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
package org.forwarder.opset;

import java.lang.reflect.Modifier;
import java.util.Map.Entry;
import java.util.Set;

import org.forwarder.Backend;
import org.forwarder.backend.BackendRegistry;
import org.forwarder.opset.annotations.Opset;
import org.onnx4j.opsets.OperatorSet;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OperatorSetLoader {

	Instance;

	private static Logger logger = LoggerFactory.getLogger(OperatorSetLoader.class);

	@SuppressWarnings("rawtypes")
	public void initialize() {
		for (Entry<String, Class<? extends Backend>> entrySet : BackendRegistry.Instance.get().entrySet()) {
			Reflections reflections = new Reflections(entrySet.getValue().getPackage().getName());
			Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Opset.class);
			for (Class<?> annotatedClass : annotatedClasses) {
				if (Modifier.isAbstract(annotatedClass.getModifiers())
						|| Modifier.isInterface(annotatedClass.getModifiers())) {
					continue;
				}

				OperatorSet opset = null;
				try {
					opset = (OperatorSet) annotatedClass.newInstance();
				} catch (Exception e) {
					logger.warn("Can not create instance for operator set by class named \"{}\", caused by {}",
							annotatedClass.getName(), e.getCause());
				}

				if (opset != null)
					OperatorSetRegistry.Instance.register(entrySet.getKey(), opset);
			}
		}
	}
}