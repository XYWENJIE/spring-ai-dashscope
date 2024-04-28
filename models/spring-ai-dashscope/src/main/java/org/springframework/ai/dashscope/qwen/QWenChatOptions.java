package org.springframework.ai.dashscope.qwen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.DashsCopeService.FunctionTool;
import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class QWenChatOptions implements FunctionCallingOptions,ChatOptions {
	
	private @JsonProperty("model") ChatModel model;
	
	private @JsonProperty("temperature") Float temperature = 0.7F;
	
	@NestedConfigurationProperty
	private @JsonProperty("tools") List<FunctionTool> tools;

	private @JsonProperty("parameters") DashsCopeService.Parameters parameters;
	
	@NestedConfigurationProperty
	@JsonIgnore
	private List<FunctionCallback> functionCallbacks = new ArrayList<>();
	
	@NestedConfigurationProperty
	@JsonIgnore
	private Set<String> functions = new HashSet<>();
	
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
		
		public Builder withTemperature(Float temperature) {
			this.options.temperature = temperature;
			return this;
		}
		
		public Builder withTools(List<FunctionTool> tools) {
			this.options.tools = tools;
			return this;
		}
		
		public Builder withFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
			this.options.functionCallbacks = functionCallbacks;
			return this;
		}
		
		public Builder withFunctions(Set<String> functionNames) {
			Assert.notNull(functionNames,"Function name must not be empty");
			this.options.functions = functionNames;
			return this;
		}
		
		public Builder withFunction(String functionName) {
			Assert.notNull(functionName, "Function name must not be empty");
			this.options.functions.add(functionName);
			return this;
		}
		
		public QWenChatOptions build() {
			this.options.parameters = new DashsCopeService.Parameters("message",null,null,null,null,null,null,null,null,null,null,0.7F,null,null,this.options.tools,null);
			return this.options;
		}
		
	}
	
	public ChatModel getModel() {
		if(model != null){
			return model;
		}
		return null;
	}

	public void setModel(ChatModel model) {
		this.model = model;
	}

	@Override
	public Float getTemperature() {
		return temperature;
	}



	@Override
	public Float getTopP() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	@JsonIgnore
	public Integer getTopK() {
		throw new UnsupportedOperationException("Unimplemented method 'getTopK'");
	}

	@Override
	public List<FunctionCallback> getFunctionCallbacks() {
		return this.functionCallbacks;
	}

	@Override
	public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
		this.functionCallbacks = functionCallbacks;
		
	}

	@Override
	public Set<String> getFunctions() {
		return functions;
	}

	@Override
	public void setFunctions(Set<String> functions) {
		this.functions = functions;
	}


}
