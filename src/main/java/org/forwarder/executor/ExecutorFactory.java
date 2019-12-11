package org.forwarder.executor;

import javax.naming.OperationNotSupportedException;

import org.forwarder.executor.impls.RayExecutor;
import org.forwarder.executor.impls.RecursionExecutor;
import org.onnx4j.Model;

public class ExecutorFactory {

	public static Executor<?> createInstance(Model model, String executorName)
			throws OperationNotSupportedException {
		if ("ray".equalsIgnoreCase(executorName)) {
			return new RayExecutor<Object>(model);
		} else if ("recursion".equalsIgnoreCase(executorName)) {
			return new RecursionExecutor<Object>(model);
		} else {
			throw new OperationNotSupportedException(
					String.format("Executor named \"%s\" not supported", executorName));
		}
	}

}
