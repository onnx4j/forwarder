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
