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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Resource<T_TS> {
	
	private Map<String, T_TS> resourceMap = new HashMap<String, T_TS>();

	public T_TS get(String name) {
		return this.resourceMap.get(name);
	}
	
	public void put(String name, T_TS tensor) {
		this.resourceMap.put(name, tensor);
	}
	
	public void merge(Resource<T_TS> otherResource) {
		this.resourceMap.putAll(otherResource.get());
	}
	
	public Map<String, T_TS> get() {
		return Collections.unmodifiableMap(this.resourceMap);
	}
	
	public Set<Entry<String, T_TS>> entrySet() {
		return this.resourceMap.entrySet();
	}

}