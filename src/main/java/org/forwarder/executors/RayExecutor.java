package org.forwarder.executors;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.forwarder.Executor;
import org.onnx4j.Inputs;
import org.onnx4j.Model;
import org.onnx4j.Outputs;
import org.onnx4j.Inputs.Input;
import org.onnx4j.Outputs.Output;
import org.onnx4j.model.Graph;
import org.onnx4j.model.graph.Node;
import org.onnx4j.model.graph.exchanges.GraphOutput;

public class RayExecutor extends Executor {
	
	private Collection<Node> orderedSequenceNodes;

	public RayExecutor(Model model) {
		this.orderedSequenceNodes = this.toOrderedSequenceNodes(model.getGraph());
	}
	
	@Override
	public void execute() throws OperationNotSupportedException {
		for (Node node : this.orderedSequenceNodes) {
			this.handle(node);
		}
		//Map<String, T_BK_TS> resourceCache;
		//return null;
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
