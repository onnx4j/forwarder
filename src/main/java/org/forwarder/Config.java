package org.forwarder;

import java.nio.ByteOrder;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.onnx4j.Tensor;
import org.onnx4j.Tensor.AllocationMode;

public class Config {

	//protected DataFormat dataFormat = DataFormat.NCHW;

	protected ByteOrder memoryByteOrder = ByteOrder.nativeOrder();

	protected AllocationMode memoryAllocationMode = AllocationMode.DIRECT;

	protected boolean isDebug = false;
	
	private Config() {};
	
	public static Config getDefaultConfigs() {
		return Config.builder().build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(String configFile) {
		try {
			return new Builder(configFile);
		} catch (ConfigurationException e) {
			return null;
		}
	}
	
	public Tensor.Options getTensorOptions() {
		return Tensor.options()
				.setAllocationMode(this.memoryAllocationMode)
				.setByteOrder(this.memoryByteOrder);
	}

	public boolean isDebug() {
		return isDebug;
	}

	//public DataFormat getDataFormat() {
	//	return dataFormat;
	//}

	public AllocationMode getMemoryAllocationMode() {
		return memoryAllocationMode;
	}

	public ByteOrder getMemoryByteOrder() {
		return memoryByteOrder;
	}

	public static class Builder {
		
		private static final String KEY_DEBUG = "debug";
		
		//private static final String KEY_DATA_FORMAT = "data_format";
		
		private static final String KEY_MEMORY_ALLOCATION_TYPE = "memory_allocation_type";
		
		private static final String KEY_MEMORY_BYTE_ORDER = "memory_byte_order";
		
		private Config config;

		public Builder() {
			this.config = new Config();
		}

		public Builder(String configFile) throws ConfigurationException {
			this();

			FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
					PropertiesConfiguration.class).configure(new Parameters().fileBased().setFileName(configFile));
			FileBasedConfiguration propConfig = builder.getConfiguration();

			//this.setDataFormat(propConfig.get(DataFormat.class, KEY_DATA_FORMAT));
			this.setMemoryAllocationMode(propConfig.get(AllocationMode.class, KEY_MEMORY_ALLOCATION_TYPE));
			this.setMemoryByteOrder(propConfig.get(ByteOrder.class, KEY_MEMORY_BYTE_ORDER));
			this.setDebug(propConfig.get(Boolean.class, KEY_DEBUG));
		}
		
		public Builder setDebug(boolean isDebug) {
			this.config.isDebug = isDebug;
			return this;
		}
		
		/*public Builder setDataFormat(DataFormat dataFormat) {
			this.config.dataFormat = dataFormat;
			return this;
		}*/
		
		public Builder setMemoryAllocationMode(AllocationMode memoryAllocationMode) {
			this.config.memoryAllocationMode = memoryAllocationMode;
			return this;
		}
		
		public Builder setMemoryByteOrder(ByteOrder memoryByteOrder) {
			this.config.memoryByteOrder = memoryByteOrder;
			return this;
		}

		public Config build() {
			return this.config;
		}

	}

}