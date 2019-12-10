package org.forwarder;

import javax.naming.OperationNotSupportedException;

import org.onnx4j.Inputs;
import org.onnx4j.Outputs;
import org.onnx4j.model.graph.Node;
import org.onnx4j.opsets.Operator;

public abstract class Executor {

	public abstract void execute() throws OperationNotSupportedException;

	public Outputs handle(Node node, Inputs inputs) throws OperationNotSupportedException {
		Operator op = this.opsets.getOperator(node.getOpType());
		if (op == null)
			throw new OperationNotSupportedException(
					String.format("Op=%s not supported in this backend", node.getOpType()));

		return op.forward(node, inputs);
	}

}
