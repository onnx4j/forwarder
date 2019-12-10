package org.forwarder.opset;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.onnx4j.opsets.OperatorSet;
import org.onnx4j.opsets.OperatorSetId;
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

	public OperatorSet get(String backendName, OperatorSetId opsetId) {
		Map<String, OperatorSet> opsets = this.opsetsGroupByBackend.get(backendName);
		if (opsets != null)
			return opsets.get(opsetId.getId());
		else
			return null;
	}

}
