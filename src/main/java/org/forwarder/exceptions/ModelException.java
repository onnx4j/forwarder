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
package org.forwarder.exceptions;

import org.onnx4j.exceptions.ErrorCode;
import org.onnx4j.exceptions.ExceptionEnums;
import org.onnx4j.exceptions.Onnx4jException;

public class ModelException extends Onnx4jException {

	private static final long serialVersionUID = -9110594428291367746L;
	
	private static final String ERR_CODE_PREFIX = "ME";
	
	private static int ERR_CODE_BASE = 1000;

	public enum ModelExceptionEnums implements ExceptionEnums {

		/**
		 * 警告：注意枚举成员的顺序，不能随意调整！
		 */
		IR_VER_UNSUPPORTED("Model's ir version(%s) is newer than supported(%s)"),
		MODEL_NOT_EXISTS("Model file not exists");

		public ErrorCode errorCode;
		public String messageTemplate;

		private ModelExceptionEnums(String messageTemplate) {
			this.errorCode = new ErrorCode(ERR_CODE_PREFIX, ERR_CODE_BASE++);
			this.messageTemplate = messageTemplate;
		}

		@Override
		public String getErrorCode() {
			return errorCode.toString();
		}

		@Override
		public String getMessageTemplate() {
			return messageTemplate;
		}

	}
	
	public ModelException(ModelExceptionEnums exceptionEnum) {
		this(exceptionEnum, new Object[] {});
	}
	
	public ModelException(ModelExceptionEnums exceptionEnum, Object... args) {
		super(exceptionEnum, args);
	}

}