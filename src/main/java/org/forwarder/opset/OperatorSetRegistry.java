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
package org.forwarder.opset;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.onnx4j.opsets.OperatorSet;
import org.onnx4j.opsets.operator.OperatorSetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OperatorSetRegistry {

	Instance;

	private static Logger logger = LoggerFactory.getLogger(OperatorSetRegistry.class);

	private AtomicBoolean atomicLock = new AtomicBoolean(false);
	private Map<String, Map<String, OperatorSet>> opsetsGroupByBackend = new HashMap<String, Map<String, OperatorSet>>();

	public void register(String backendName, OperatorSet opset) {
		if (this.atomicLock.compareAndSet(false, true)) {
			try {
				Map<String, OperatorSet> opsets = opsetsGroupByBackend.get(backendName);
				if (opsets == null) {
					opsets = new HashMap<String, OperatorSet>();
					this.opsetsGroupByBackend.put(backendName, opsets);
				}
				opsets.put(opset.getId(), opset);
			} finally {
				this.atomicLock.set(false);
				logger.info("Opset \"{}\" in \"{}\" has installed", opset.getId(), backendName);
			}
		} else {
			throw new ConcurrentModificationException("OperatorSet register conflict.");
		}
	}

	public Collection<OperatorSet> get(String backendName) {
		Map<String, OperatorSet> opsets = this.opsetsGroupByBackend.get(backendName);
		if (opsets != null)
			return Collections.unmodifiableCollection(opsets.values());
		else
			return null;
	}

	public OperatorSet get(String backendName, OperatorSetId opsetId) {
		Map<String, OperatorSet> opsets = this.opsetsGroupByBackend.get(backendName);
		if (opsets != null)
			return opsets.get(opsetId.getId());
		else
			return null;
	}

}