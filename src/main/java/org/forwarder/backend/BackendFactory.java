package org.forwarder.backend;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.forwarder.Backend;
import org.forwarder.Model;

public class BackendFactory {

	public static Backend createInstance(String backendName, Model model)
			throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<? extends Backend> classOfBackend = BackendRegistry.Instance
				.get(backendName);
		if (classOfBackend == null)
			throw new IllegalArgumentException(String.format(
					"Backend named \"%s\" not found.", backendName));

		Constructor<? extends Backend> constructorOfBackend = classOfBackend
				.getConstructor(Model.class);
		assert constructorOfBackend != null;

		return constructorOfBackend.newInstance(model);
	}

}
