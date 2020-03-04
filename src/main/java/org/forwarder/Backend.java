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

import org.forwarder.opset.OperatorSetRegistry;
import org.onnx4j.Tensor;
import org.onnx4j.TensorManager;
import org.onnx4j.model.graph.Constant;
import org.onnx4j.opsets.OperatorSet;
import org.onnx4j.opsets.OperatorSets;
import org.onnx4j.opsets.operator.OperatorSetId;

public abstract class Backend<T_TS> implements AutoCloseable {

	protected Model model;
	protected TensorManager<T_TS> tensorManager;
	protected OperatorSets opsets;

	public Backend() {
	}

	public Backend(Model model) {
		this(model.getOpsetIds());
		this.model = model;
		this.tensorManager = new TensorManager<T_TS>() {

			@Override
			protected void dispose(T_TS tensor) {
				disposeBackendTensor(tensor);
			}

		};
		this.initConstants(this.model.getGraph().getConstants());
	}

	public Backend(OperatorSetId[] opsetIds) {
		this.opsets = this.getOpsets(opsetIds);
	}

	public Model getModel() {
		return model;
	}

	@Override
	public void close() throws Exception {
		this.tensorManager.close();
		this.model.closeBackend(this.getName());
	}

	public TensorManager<T_TS> getTensorManager() {
		return tensorManager;
	}

	public OperatorSets getOpsets() {
		return this.opsets;
	}

	protected OperatorSets getOpsets(OperatorSetId[] opsetIds) {
		OperatorSet[] opsets = new OperatorSet[opsetIds.length];

		for (int n = 0; n < opsetIds.length; n++) {
			opsets[n] = OperatorSetRegistry.Instance.get(this.getName(), opsetIds[n]);
			assert opsets[n] != null;
		}

		return OperatorSets.wrap(opsets);
	}

	private void initConstants(Constant[] contants) {
		for (Constant constant : contants) {
			this.toBackendTensor(this.tensorManager, constant.getTensor());
		}
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
	public abstract T_TS toBackendTensor(TensorManager<T_TS> tensorManager, Tensor tensor);

	public abstract Tensor toNativeTensor(TensorManager<Tensor> tensorManager, String name, T_TS tensor);

	public abstract Session<T_TS> newSession();

}