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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.forwarder.Backend;
import org.forwarder.Model;

public class BackendFactory {

	@SuppressWarnings("rawtypes")
	public static Backend<?> createInstance(String backendName, Model model)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<? extends Backend> classOfBackend = BackendRegistry.Instance.get(backendName);
		if (classOfBackend == null)
			throw new IllegalArgumentException(String.format("Backend named \"%s\" not found.", backendName));

		Constructor<? extends Backend> constructorOfBackend = classOfBackend.getConstructor(Model.class);
		assert constructorOfBackend != null;

		return constructorOfBackend.newInstance(model);
	}

}