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
package org.forwarder.demo.opset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.forwarder.opset.OperatorSetRegistry;
import org.forwarder.opset.operator.Executable;
import org.junit.Test;
import org.onnx4j.Inputs;
import org.onnx4j.Outputs;
import org.onnx4j.model.graph.Node;
import org.onnx4j.opsets.Operator;
import org.onnx4j.opsets.OperatorSet;
import org.onnx4j.opsets.operator.OperatorInputs;
import org.onnx4j.opsets.operator.OperatorOutputs;
import org.onnx4j.opsets.operator.OperatorSetId;

public class OpsetTest {

	@Test
	public void testRegisterNewOpset() {
		String backendName = "DemoBackend";
		String domain = "domain.demo";
		long opsetVer = 1;
		OperatorSetRegistry.Instance.register(backendName,
				new OperatorSet(0, null, null, domain, opsetVer, "Just for test") {
				});
		OperatorSet opset = OperatorSetRegistry.Instance.get(backendName, new OperatorSetId(domain, opsetVer));
		assertNotNull(opset);
		assertEquals(domain, opset.getDomain());
		assertEquals(opsetVer, opset.getOpsetVersion());
	}

	@Test
	public void testRegisterNewOp() throws Exception {
		String backendName = "DemoBackend";
		String domain = "domain.demo";
		long opsetVer = 1;
		String newOpType = "TestOp";
		//float testData = 15.23f;

		class TestOperatorOutputs extends OperatorOutputs<Object> {

			@Override
			public Outputs toOutputs(Node node) {
				// TODO Auto-generated method stub
				return null;
			}

		}

		class TestOperator implements Operator, Executable<Object> {

			@Override
			public long getVersion() {
				return opsetVer;
			}

			@Override
			public long getSinceVersion() {
				return opsetVer;
			}

			@Override
			public OperatorStatus getStatus() {
				return OperatorStatus.EXPERIMENTAL;
			}

			@Override
			public String getOpType() {
				return newOpType;
			}

			@Override
			public void preconditions(OperatorInputs<Object> operatorInputs) {
				// TODO Auto-generated method stub

			}

			@Override
			public OperatorOutputs<Object> forward(Node node, Inputs inputs) {
				return new TestOperatorOutputs();
			}

		}

		OperatorSetRegistry.Instance.register(backendName,
				new OperatorSet(0, null, null, domain, opsetVer, "Just for test") {
				});
		OperatorSet opset = OperatorSetRegistry.Instance.get(backendName, new OperatorSetId(domain, opsetVer));
		opset.registerOperator(new TestOperator());
		Operator testOp = opset.getOp(newOpType);
		assertNotNull(testOp);
		/*Executable<?> execOp = Executable.class.cast(testOp);
		Node node = new Node(null, NodeProto.newBuilder().setName("node1").addOutput("output1").build(), null);
		Outputs outputs = execOp.forward(execOp.asOperatorInputs(node, null)).toOutputs(node);
		assertNotNull(outputs);
		Tensor ts = outputs.get()[0].getTensor();
		assertEquals(testData, ts.getData().asFloatBuffer().get(), 0f);*/
	}

}