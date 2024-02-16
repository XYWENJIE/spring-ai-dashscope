package org.springframework.ai.dashcope.metadata.support;

public enum ChatModel {
	QWen_1_8B_CHAT("qwen-1.8b-chat"),QWen_72B_CHAT("qwen-72b-chat");
	
	private String modelValue;

	ChatModel(String modelValue) {
		this.modelValue = modelValue;
	}
	
	public String getModelValue() {
		return modelValue;
	}

}
