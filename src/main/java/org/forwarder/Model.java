package org.forwarder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.onnx4j.Tensor;
import org.onnx4j.model.Graph;
import org.onnx4j.model.graph.Node;
import org.onnx4j.model.graph.exchanges.GraphOutput;

public class Model extends org.onnx4j.Model {

	private Collection<Node> orderedSequenceNodes;

	public Model(String onnxModelPath) throws FileNotFoundException, IOException {
		this(onnxModelPath, Tensor.options());
	}

	public Model(String onnxModelPath, Tensor.Options tensorOptions) throws FileNotFoundException, IOException {
		super(onnxModelPath, tensorOptions);

		this.orderedSequenceNodes = this.toOrderedSequenceNodes(super.getGraph());
	}

	public Collection<Node> getOrderedSequenceNodes() {
		return this.orderedSequenceNodes;
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
