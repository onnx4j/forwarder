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
package org.forwarder;

import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.forwarder.executor.Executor;
import org.onnx4j.Outputs;
import org.onnx4j.Outputs.Output;
import org.onnx4j.Tensor;
import org.onnx4j.TensorManager;
import org.onnx4j.model.graph.exchanges.GraphInput;
import org.onnx4j.model.graph.exchanges.GraphOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Session<T_BK_TS> implements AutoCloseable {

	protected static final ThreadLocal<Session<?>> TL_SESSION = new ThreadLocal<Session<?>>();

	private static Logger logger = LoggerFactory.getLogger(Session.class);

	protected Executor<T_BK_TS> executor;
	protected Backend<T_BK_TS> backend;
	protected Outputs outputs;
	protected Map<String, T_BK_TS> intermediateOutputs;
	protected TensorManager<T_BK_TS> intermediateTensorManager;
	protected TensorManager<Tensor> exchangeTensorManager;

	public Session(Executor<T_BK_TS> executor, Backend<T_BK_TS> backend) {
		if (Session.TL_SESSION.get() != null)
			throw new RuntimeException("Session in this thread has been inited");

		Session.TL_SESSION.set(this);

		this.executor = executor;
		this.backend = backend;
		this.intermediateOutputs = new HashMap<String, T_BK_TS>();
		this.outputs = new Outputs();

		this.intermediateTensorManager = new TensorManager<T_BK_TS>() {

			@Override
			protected void dispose(T_BK_TS tensor) {
				backend.disposeBackendTensor(tensor);
			}

		};

		this.exchangeTensorManager = new TensorManager<Tensor>() {

			@Override
			protected void dispose(Tensor tensor) {
				tensor.close();
			}

		};

		logger.debug("Session binded in thread \"{}\"", Thread.currentThread().getName());
	}

	public Session<T_BK_TS> feed(Tensor tensor) {
		return this.feed(tensor, true);
	}

	public Session<T_BK_TS> feed(Tensor tensor, boolean autoAttach) {
		if (tensor.getName() != null)
			return this.feed(tensor.getName(), tensor, autoAttach);
		else
			throw new IllegalArgumentException("The name of tensor can not be null.");
	}

	public Session<T_BK_TS> feed(String name, Tensor tensor) {
		return this.feed(name, tensor, true);
	}

	public Session<T_BK_TS> feed(String name, Tensor tensor, boolean autoAttach) {
		GraphInput graphInput = this.backend.getModel().getGraph().getInputs(name);
		if (graphInput == null) {
			throw new IllegalArgumentException(String.format("Input named \"%s\" had not be defined in graph", name));
		} else {
			if (!tensor.equals(graphInput.getValueInfo())) {
				throw new IllegalArgumentException(
						String.format("Shape or DataType is not equals to the input tensor named \"%s\" ", name));
			}
		}

		T_BK_TS backendTensor = this.backend.toBackendTensor(this.intermediateTensorManager, tensor);

		this.intermediateOutputs.put(name, backendTensor);
		
		if (autoAttach) {
			this.exchangeTensorManager.attach(name, tensor);
		}

		return this;
	}

	public Session<T_BK_TS> forward() throws OperationNotSupportedException {
		//
		// Put all constant resources to session
		//
		this.intermediateOutputs.putAll(this.backend.getTensorManager().get());

		this.executor.execute(this, this.backend.getOpsets());

		for (GraphOutput graphOutput : this.backend.getModel().getGraph().getOutputs()) {
			T_BK_TS backendTensor = this.intermediateOutputs.get(graphOutput.getName());
			Tensor tensor = this.backend.toNativeTensor(this.exchangeTensorManager, graphOutput.getName(),
					backendTensor);
			Output output = Output.wrap(graphOutput.getName(), tensor);
			outputs.append(graphOutput.getName(), output);
		}
		return this;
	}

	public TensorManager<T_BK_TS> getTensorManager() {
		return intermediateTensorManager;
	}

	public Backend<T_BK_TS> getBackend() {
		return this.backend;
	}

	public Tensor getOutput(String name) {
		return this.outputs.getTensor(name);
	}

	public void putIntermediateOutput(String name, T_BK_TS backendTensor) {
		this.intermediateTensorManager.attach(name, backendTensor);
		this.intermediateOutputs.put(name, backendTensor);
	}

	public T_BK_TS getIntermediateOutput(String name) {
		return this.intermediateOutputs.get(name);
	}

	@Override
	public void close() throws Exception {
		this.intermediateTensorManager.close();
		this.exchangeTensorManager.close();

		Session.TL_SESSION.remove();
		logger.debug("Session closed");
	}

}