package org.forwarder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.forwarder.backend.BackendFactory;
import org.forwarder.executor.Executor;
import org.forwarder.executor.ExecutorFactory;
import org.onnx4j.prototypes.OnnxProto3.ModelProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model extends org.onnx4j.Model {

	private static Logger logger = LoggerFactory.getLogger(Model.class);

	private String id;
	private Config config;
	private Executor<?> executor;
	private Map<String, Backend<?>> backends = new HashMap<String, Backend<?>>();

	public Model(String id, String onnxModelPath, Config config)
			throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(onnxModelPath, config.getTensorOptions());

		this.executor(config.getExecutor());
		this.config = config;
		this.id = id;
	}

	public Model(String id, ModelProto onnxModel, Config config) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(onnxModel, config.getTensorOptions());

		this.executor(config.getExecutor());
		this.config = config;
		this.id = id;
	}

	public Backend<?> backend(String name) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (this.backends.containsKey(name)) {
			return this.backends.get(name);
		} else {
			Backend<?> backend = BackendFactory.createInstance(name, this);
			assert backend != null;
			this.backends.put(name, backend);
			return backend;
		}
	}

	public Model executor(Class<? extends Executor> classOfExecutor) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.executor = ExecutorFactory.createInstance(this, classOfExecutor);
		return this;
	}

	public <T_BK_TS> Executor<T_BK_TS> getExecutor() {
		return (Executor<T_BK_TS>) this.executor;
	}

	public Config getConfig() {
		return this.config;
	}

	public String getId() {
		return id;
	}

	@Override
	public void close() throws Exception {
		for (Entry<String, Backend<?>> backend : backends.entrySet()) {
			backend.getValue().close();

			logger.debug("Backend named {} has been closed", backend.getKey());
		}

		this.backends.clear();
		this.backends = null;

		super.close();
	}
	
	protected void closeBackend(String backendName) {
		this.backends.remove(backendName);
	}

}
