package org.forwarder;

import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.onnx4j.Inputs;
import org.onnx4j.Inputs.Input;
import org.onnx4j.Outputs;
import org.onnx4j.Outputs.Output;
import org.onnx4j.Tensor;
import org.onnx4j.model.graph.Node;
import org.onnx4j.model.graph.exchanges.GraphInput;
import org.onnx4j.model.graph.exchanges.GraphOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Session<T_BK_TS> implements AutoCloseable {

	protected static final ThreadLocal<Session<?>> TL_SESSION = new ThreadLocal<Session<?>>();

	private static Logger logger = LoggerFactory.getLogger(Session.class);

	protected Backend<T_BK_TS> backend;
	protected Map<String, T_BK_TS> resourceCache;
	protected Outputs outputs;

	public Session(Backend<T_BK_TS> backend) {
		if (Session.TL_SESSION.get() != null)
			throw new RuntimeException("Session in this thread has been inited");

		this.backend = backend;
		this.resourceCache = new HashMap<String, T_BK_TS>();
		this.outputs = new Outputs();

		Session.TL_SESSION.set(this);

		logger.debug("Session binded in thread \"{}\"", Thread.currentThread().getName());
	}

	public Session<T_BK_TS> feed(String name, Tensor tensor) {
		GraphInput graphInput = this.backend.getModel().getGraph().getInputs(name);
		if (graphInput == null) {
			throw new IllegalArgumentException(String.format("Input named \"%s\" had not be defined in graph", name));
		} else {
			if (!tensor.equals(graphInput.getValueInfo())) {
				throw new IllegalArgumentException(
						String.format("Shape or DataType is not equals to the input tensor named \"%s\" ", name));
			}
		}

		T_BK_TS backendTensor = this.backend.toBackendTensor(tensor);

		this.resourceCache.put(name, backendTensor);

		//
		// 完成转换后，将原生Tensor资源释放
		//
		// tensor.close();

		return this;
	}

	public Session<T_BK_TS> forward() throws OperationNotSupportedException {
		//
		// Put all constant resources to session
		//
		this.resourceCache.putAll(this.backend.getResourceCache());

		for (Node node : this.backend.getModel().getOrderedSequenceNodes()) {
			this.handle(node);
		}

		for (GraphOutput graphOutput : this.backend.getModel().getGraph().getOutputs()) {
			T_BK_TS backendTensor = this.resourceCache.get(graphOutput.getName());
			Tensor tensor = this.backend.toTensor(backendTensor);
			Output output = Output.wrap(graphOutput.getName(), tensor);
			outputs.append(graphOutput.getName(), output);
		}
		return this;
	}

	public Tensor getOutput(String name) {
		return this.outputs.getTensor(name);
	}

	public Backend<T_BK_TS> getBackend() {
		return this.backend;
	}

	@Override
	public void close() {
		Session.TL_SESSION.remove();
		logger.debug("Session closed");
	}

	private void handle(Node node) throws OperationNotSupportedException {
		Inputs inputs = new Inputs();
		for (String inputName : node.getInputNames()) {
			Input input = Input.wrap(inputName, node, this.resourceCache.get(inputName));
			inputs.append(input);
		}
		Outputs outputs = backend.handle(node, inputs);
		for (Output output : outputs.get()) {
			this.resourceCache.put(output.getName(), output.getTensor());
		}
	}

}
