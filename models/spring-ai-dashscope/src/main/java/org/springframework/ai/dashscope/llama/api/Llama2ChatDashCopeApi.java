package org.springframework.ai.dashscope.llama.api;

import org.springframework.ai.dashscope.api.AbstractDashScopeService;
import org.springframework.ai.dashscope.llama.api.Llama2ChatDashCopeApi.Llama2ChatRequest;
import org.springframework.ai.dashscope.llama.api.Llama2ChatDashCopeApi.Llama2ChatResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.model.ModelDescription;

public class Llama2ChatDashCopeApi extends AbstractDashScopeService<Llama2ChatRequest, Llama2ChatResponse, Llama2ChatResponse> {

	public Llama2ChatDashCopeApi(String accessToken, String requestUrl) {
		super(accessToken, requestUrl);
	}

	@JsonInclude(Include.NON_NULL)
	public record Llama2ChatRequest(
			@JsonProperty("model") LlamaChatModel model,
			@JsonProperty("input") Input input) {
		
		public static Builder builder(String prompt) {
			return new Builder(prompt);
		}
		
		public static class Builder {
			
			private String prompt;
			
			public Builder(String prompt) {
				this.prompt = prompt;
			}
			
			public Llama2ChatRequest build() {
				return null;
			}
		}
	}
	
	public record Input(
			@JsonProperty("prompt") String prompt) {
		
	}
	
	public record Llama2ChatResponse(
			@JsonProperty("output") Output output,
			@JsonProperty("usage") Usage usage,
			@JsonProperty("request_id") String requestId) {
		
	}
	
	public record Output(@JsonProperty("text") String text) {
		
	}
	
	public record Usage(
			@JsonProperty("output_tokens")  Integer outputTokens,
			@JsonProperty("input_tokens") Integer inputTokens) {
		
	}

	public enum LlamaChatModel implements ModelDescription{
		LLAMA2_8B_INSTRUCT("llama2-8b-instruct");

		private final String id;

		LlamaChatModel(String id) {
			this.id = id;
		}

		@Override
		public String getModelName() {
			return this.id;
		}
	}
	
	protected Llama2ChatResponse chatCompletion(Llama2ChatRequest request) {
		return this.internalInvocation(request,Llama2ChatResponse.class);
	};

}
