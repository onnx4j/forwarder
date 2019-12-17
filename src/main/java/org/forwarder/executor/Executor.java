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