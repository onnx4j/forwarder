package org.forwarder;

public abstract class Resource<T_TS> {
	
	private T_TS tensor;

	public T_TS getTensor() {
		return tensor;
	}

}
