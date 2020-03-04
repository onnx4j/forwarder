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
import java.util.LinkedList;

import org.forwarder.Session;
import org.forwarder.executor.Executor;
import org.onnx4j.Inputs;
import org.onnx4j.Inputs.Input;
import org.onnx4j.Model;
import org.onnx4j.Outputs;
import org.onnx4j.Outputs.Output;
import org.onnx4j.model.Graph;
import org.onnx4j.model.graph.Node;
import org.onnx4j.model.graph.exchanges.GraphOutput;
import org.onnx4j.opsets.OperatorSets;

public class RayExecutor<T_BK_TS> extends Executor<T_BK_TS> {

	private Collection<Node> orderedSequenceNodes;

	public RayExecutor(Model model) {
		super(model);
		this.orderedSequenceNodes = this.toOrderedSequenceNodes(model.getGraph());
	}

	@Override
	public void execute(Session<T_BK_TS> session, OperatorSets opsets) {
		for (Node node : this.orderedSequenceNodes) {
			this.handle(session, opsets, node);
		}
	}

	private void handle(Session<T_BK_TS> session, OperatorSets opsets, Node node) {
		Inputs inputs = new Inputs();
		for (String inputName : node.getInputNames()) {
			Input input = Input.wrap(inputName, node, session.getIntermediateOutput(inputName));
			inputs.append(input);
		}
		Outputs outputs = super.handle(session, opsets, node, inputs);
		for (Output output : outputs.get()) {
			session.putIntermediateOutput(output.getName(), output.getTensor());
		}
	}

	private Collection<Node> toOrderedSequenceNodes(Graph graph) {
		Collection<Node> nodes = new LinkedList<Node>();
		for (GraphOutput graphOutput : graph.getOutputs()) {
			this.predecessors(nodes, graph, graphOutput.getNode());
			this.addOrderedSequenceNode(nodes, graphOutput.getNode());
		}
		return nodes;
	}

	private void predecessors(Collection<Node> nodes, Graph graph, Node node) {
		Collection<Node> set = graph.predecessors(node);
		for (Node predecessorNode : set) {
			this.predecessors(nodes, graph, predecessorNode);
		}

		for (Node predecessor : set) {
			this.addOrderedSequenceNode(nodes, predecessor);
		}
	}

	private void addOrderedSequenceNode(Collection<Node> nodes, Node node) {
		if (this.contains(nodes, node) == false)
			nodes.add(node);
	}

	private boolean contains(Collection<Node> nodes, Node node) {
		for (Node existingNode : nodes) {
			if (node.equals(existingNode))
				return true;
		}

		return false;
	}

}