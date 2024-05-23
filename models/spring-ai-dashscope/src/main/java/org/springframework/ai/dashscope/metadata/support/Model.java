package org.springframework.ai.dashscope.metadata.support;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Model {
	QWen_1_8B_CHAT("qwen-1.8b-chat"),
	QWen_1_8B_LONGCONTEXT_CHAT("qwen-1.8b-longcontext-chat"),
	QWen_7B_CHAT("qwen-7b-chat"),
	QWen_14B_CHAT("qwen-14b-chat"),
	QWen_72B_CHAT("qwen-72b-chat"),
	QWen1_5_7B_CHAT("qwen1.5-7b-chat"),
	QWen1_5_14B_CHAT("qwen1.5-14b-chat"),
	QWen1_5_72B_CHAT("qwen1.5-72b-chat"),

	QWen_TURBO("qwen-turbo"),
	QWen_VL_PLUS("qwen-vl-plus"),
	QWen_VL_MAX("qwen-vl-max");
	
	private String modelValue;

	Model(String modelValue) {
		this.modelValue = modelValue;
	}

	@JsonValue
	public String getModelValue() {
		return modelValue;
	}

}
