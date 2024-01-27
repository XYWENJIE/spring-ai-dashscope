package org.springframework.ai.dashcope;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

public class DashCopeService {

	private final String baseUrl = "https://dashscope.aliyuncs.com";

	private final RestClient restClient;
	
	private final WebClient webClient;

	public DashCopeService(String accessToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.webClient = WebClient.builder().baseUrl(baseUrl).build();
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
			@JsonProperty("model") String model,
			@JsonProperty("input") Input input,
			@JsonProperty("parameters") Parameters parameters) {
		
		public ChatCompletionRequest(List<ChatCompletionMessage> messages,String model,Float temperature) {
			this(model,new Input(null, messages),new Parameters(null, null, null, null, null, null, temperature, null, null));
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Input(
			@JsonProperty("prompt") String prompt,
			@JsonProperty("messages") List<ChatCompletionMessage> messages) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
			@JsonProperty("role")String role,
			@JsonProperty("content")String content) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Parameters(
			@JsonProperty("result_format")String resultFormat,
			@JsonProperty("seed")Integer seed,
			@JsonProperty("max_tokens")Integer maxTokens,
			@JsonProperty("top_p")Float TopP,
			@JsonProperty("top_k")Integer topK,
			@JsonProperty("repetition_penalty")Float repetitionPenlty,
			@JsonProperty("temperature") Float temperature,
			@JsonProperty("stop")List<String> stop,
			@JsonProperty("incremental_output")Boolean incrementalOutput) {	
	}
	
	@JsonInclude(Include.NON_NULL)
	public record ChatCompletion(
			@JsonProperty("output") Output output,
			@JsonProperty("usage") Usage usage,
			@JsonProperty("request_id") String requestId){}
	
	@JsonInclude(Include.NON_NULL)
	public record Output(
			@JsonProperty("text")String text,
			@JsonProperty("finish_reason") String finishReason,
			@JsonProperty("choise")List<Choise> choise) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Choise(
			@JsonProperty("finish_reason")String finishReason,
			@JsonProperty("message") ChatCompletionMessage message) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Usage(
			@JsonProperty("output_tokens")Integer outputTokens,
			@JsonProperty("input_tokens")Integer inputToken) {
	}
	
	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest){
		Assert.notNull(chatRequest, "请求体不能为空。");
		return this.restClient.post()
				.uri("/api/v1/services/aigc/text-generation/generation")
				.body(chatRequest).retrieve().toEntity(ChatCompletion.class);
	}
	
	public Flux<ChatCompletion> chatCompletionStream(ChatCompletionRequest chatRequest){
		Assert.notNull(chatRequest, "请求体不能为空。");
		return this.webClient.post()
				.uri("/api/v1/services/aigc/text-generation/generation")
				.body(chatRequest,ChatCompletion.class).retrieve().bodyToFlux(ChatCompletion.class);
	}
	
//	public static void main(String[] args) throws JsonProcessingException {
//		List<Message> messages = new ArrayList();
//		messages.add(new Message("system", "You are a helpful assistant."));
//		Input input = new Input("哪个公园距离我更近",messages);
//		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest("dsadsa", input);
//		ObjectMapper objectMapper = new ObjectMapper();
//		String body = objectMapper.writeValueAsString(chatCompletionRequest);
//		System.out.println(body);
//	}

}
