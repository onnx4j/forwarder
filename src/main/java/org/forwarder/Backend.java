package org.forwarder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

import org.forwarder.opset.OperatorSetRegistry;
import org.onnx4j.Inputs;
import org.onnx4j.Outputs;
import org.onnx4j.Tensor;
import org.onnx4j.model.graph.Constant;
import org.onnx4j.model.graph.Node;
import org.onnx4j.opsets.Operator;
import org.onnx4j.opsets.OperatorSet;
import org.onnx4j.opsets.OperatorSetId;
import org.onnx4j.opsets.OperatorSets;

public abstract class Backend<T_TS> implements AutoCloseable {

	private Model model;
	private OperatorSets opsets;
	private Map<String, T_TS> resourceCache = new HashMap<String, T_TS>();

	public Backend() {
	}

	public Backend(Model model) {
		this(model.getOpsetIds());
		this.model = model;

		this.resourceCache.putAll(this.initConstants(this.model.getGraph().getConstants()));
	}

	public Backend(OperatorSetId[] opsetIds) {
		this.opsets = this.getOpsets(opsetIds);
	}

	public Outputs handle(Node node, Inputs inputs) throws OperationNotSupportedException {
		Operator op = this.opsets.getOperator(node.getOpType());
		if (op == null)
			throw new OperationNotSupportedException(
					String.format("Op=%s not supported in this backend", node.getOpType()));

		return op.forward(node, inputs);
	}

	public Model getModel() {
		return model;
	}

	public Map<String, T_TS> getResourceCache() {
		return resourceCache;
	}

	@Override
	public void close() {
		for (Entry<String, T_TS> entryset : resourceCache.entrySet()) {
			this.disposeBackendTensor(entryset.getValue());
		}
	}

	protected OperatorSets getOpsets(OperatorSetId[] opsetIds) {
		OperatorSet[] opsets = new OperatorSet[opsetIds.length];

		for (int n = 0; n < opsetIds.length; n++) {
			opsets[n] = OperatorSetRegistry.Instance.get(this.getName(), opsetIds[n]);
			assert opsets[n] != null;
		}

		return OperatorSets.wrap(opsets);
	}

	private Map<String, T_TS> initConstants(Constant[] contants) {
		Map<String, T_TS> constantResources = new HashMap<String, T_TS>();
		for (Constant constant : contants) {
			constantResources.put(constant.getName(), this.toBackendTensor(constant.getTensor()));
		}
		return constantResources;
	}

	/**
	 * Backend名称，如： Tensorflow、DL4J等。
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * 关闭/释放后端实现Tensor资源
	 * 
	 * @param tensor
	 *            后端实现Tensor
	 */
	public abstract void disposeBackendTensor(T_TS tensor);

	/**
	 * 将系统原生Tensor资源转换为后端实现的Tensor资源
	 * 
	 * @param tensor
	 *            原生Tensor资源
	 * @return 后端实现的Tensor资源
	 */
	public abstract T_TS toBackendTensor(Tensor tensor);

	public abstract Tensor toTensor(T_TS tensor);

	public abstract Session<T_TS> newSession();

}
