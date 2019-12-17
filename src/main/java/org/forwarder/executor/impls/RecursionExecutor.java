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
package org.forwarder.executor.impls;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import org.forwarder.Session;
import org.forwarder.executor.Executor;
import org.onnx4j.Inputs;
import org.onnx4j.Inputs.Input;
import org.onnx4j.Model;
import org.onnx4j.Outputs;
import org.onnx4j.Outputs.Output;
import org.onnx4j.model.graph.Node;
import org.onnx4j.model.graph.exchanges.GraphOutput;
import org.onnx4j.opsets.OperatorSets;

public class RecursionExecutor<T_BK_TS> extends Executor<T_BK_TS> {

	public RecursionExecutor(Model model) {
		super(model);
	}

	@Override
	public void execute(Session<T_BK_TS> session, OperatorSets opsets) throws OperationNotSupportedException {
		for (GraphOutput graphOutput : super.model.getGraph().getOutputs()) {
			this.handle(session, opsets, graphOutput.getNode());
		}
	}

	private void handle(Session<T_BK_TS> session, OperatorSets opsets, Node node)
			throws OperationNotSupportedException {
		Inputs inputs = new Inputs();
		for (String inputName : node.getInputNames()) {
			T_BK_TS inputTensor = session.getResourceCache(inputName);
			if (inputTensor == null) {
				Collection<Node> predecessors = super.model.getGraph().predecessors(node);
				for (Node predecessor : predecessors) {
					this.handle(session, opsets, predecessor);
				}
				inputTensor = session.getResourceCache(inputName);
			}
			Input input = Input.wrap(inputName, node, inputTensor);
			inputs.append(input);
		}
		Outputs outputs = super.handle(session, opsets, node, inputs);
		for (Output output : outputs.get()) {
			session.putResourceCache(output.getName(), output.getTensor());
		}
	}

}