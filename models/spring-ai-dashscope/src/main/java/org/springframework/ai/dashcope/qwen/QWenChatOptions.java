package org.springframework.ai.dashcope.qwen;

import org.springframework.ai.chat.ChatOptions;
import org.springframework.ai.dashcope.metadata.support.ChatModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class QWenChatOptions implements ChatOptions{
	
	private @JsonProperty("model") ChatModel model;
	
	private @JsonProperty("temperature") Float temperature = 0.7F;
	
	public static Builder builder() {
		return new Builder();
	}
	

	public static class Builder {
		
		protected QWenChatOptions options;
		
		public Builder() {
			this.options = new QWenChatOptions();
		}
		
		public Builder withModel(ChatModel model) {
			this.options.model = model;
			return this;
		}
		
		public Builder withTemperature(Float templerature) {
			this.options.temperature = templerature;
			return this;
		}
		
		public QWenChatOptions build() {
			return this.options;
		}
		
	}
	
	public String getModel() {
		return model.getModelValue();
	}

	public void setModel(ChatModel model) {
		this.model = model;
	}

	@Override
	public Float getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}

	@Override
	public Float getTopP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTopP(Float topP) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer getTopK() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTopK(Integer topK) {
		// TODO Auto-generated method stub
		
	}

}
