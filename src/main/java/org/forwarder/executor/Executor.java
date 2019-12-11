package org.forwarder.executor;

import javax.naming.OperationNotSupportedException;

import org.forwarder.Session;
import org.onnx4j.Inputs;
import org.onnx4j.Model;
import org.onnx4j.Outputs;
import org.onnx4j.model.graph.Node;
import org.onnx4j.opsets.Operator;
import org.onnx4j.opsets.OperatorSets;

public abstract class Executor<T_BK_TS> {

	protected Model model;
	protected OperatorSets opsets;

	public Executor(Model model) {
		this.model = model;
	}

	public abstract void execute(Session<T_BK_TS> session, OperatorSets opsets) throws OperationNotSupportedException;

	public Outputs handle(Session<T_BK_TS> session, OperatorSets opsets, Node node, Inputs inputs)
			throws OperationNotSupportedException {
		Operator op = opsets.getOperator(node.getOpType());
		if (op == null)
			throw new OperationNotSupportedException(
					String.format("Op=%s not supported in this backend", node.getOpType()));

		return op.forward(node, inputs);
	}

}
