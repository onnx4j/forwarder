package org.forwarder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

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

	public Forwarder executor(String name) throws OperationNotSupportedException {
		this.executor = ExecutorFactory.createInstance(model, name);
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
