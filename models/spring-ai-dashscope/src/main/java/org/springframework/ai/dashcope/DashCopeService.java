package org.springframework.ai.dashcope;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

public class DashCopeService {
	
	private final Logger logger = LoggerFactory.getLogger(DashCopeService.class);

	private final String baseUrl = "https://dashscope.aliyuncs.com";

	private final RestClient restClient;
	
	private final WebClient webClient;
	
	private final ObjectMapper objectMapper;

	public DashCopeService(String accessToken) {
		
		this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.setBearerAuth(accessToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};
		
		var responseErrorHandler = new ResponseErrorHandler() {
			
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return response.getStatusCode().isError();
			}
			
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				if(response.getStatusCode().isError()) {
					if(response.getStatusCode().is4xxClientError()) {
						throw new DashCopeApiClientErrorException(String.format("%s - %s",response.getStatusCode().value(),
								objectMapper.readValue(response.getBody(), ResponseError.class)));
					}
					throw new DashCopeApiException(String.format("%s - %s", response.getStatusCode().value(),
							objectMapper.readValue(response.getBody(), ResponseEntity.class)));
				}
			}
		};
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeaders(jsonContentHeaders)
				//.requestInterceptor(new LogHttpRequestInterceptor())
				.defaultStatusHandler(responseErrorHandler)
				.build();
		this.webClient = WebClient.builder().baseUrl(baseUrl).build();
	}
	
	public static class DashCopeApiException extends RuntimeException {
		public DashCopeApiException(String message) {
			super(message);
		}
	}
	
	public static class DashCopeApiClientErrorException extends RuntimeException {
		
		public DashCopeApiClientErrorException(String message) {
			super(message);
		}
		
		public DashCopeApiClientErrorException(String message,Throwable cause) {
			super(message,cause);
		}
	}
	
	public class LogHttpRequestInterceptor implements ClientHttpRequestInterceptor{
		
		private final Logger logger = LoggerFactory.getLogger(DashCopeService.class);

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			logger.info("Body:{}",new String(body));
			ClientHttpResponse httpResponse = execution.execute(request, body);
			logger.info("Http Response:");
			HttpHeaders httpHeaders = httpResponse.getHeaders();
			httpHeaders.forEach((t, u) -> {
				logger.info("Http Header:{},value:{}",t,u);
			});
			logger.info("Http Response Code: {}",httpResponse.getStatusCode());
			if(httpResponse.getBody() != null) {
				try (ByteArrayInputStream bais = new ByteArrayInputStream(StreamUtils.copyToByteArray(httpResponse.getBody()))){
					String responseBody = new String(bais.readAllBytes(),StandardCharsets.UTF_8);
					logger.info("Response Body : {}",responseBody);
					ChatCompletion chatCompletion = objectMapper.readValue(responseBody, ChatCompletion.class);
					logger.info(chatCompletion.toString());
				}
			}
			return httpResponse;
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	public record ResponseError(
			@JsonProperty("code")String code,
			@JsonProperty("message") String message,
			@JsonProperty("request_id")String requestId) {}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
			@JsonProperty("model") String model,
			@JsonProperty("input") Input input,
			@JsonProperty("parameters") Parameters parameters) {
		
		public ChatCompletionRequest(List<ChatCompletionMessage> messages,String model,Float temperature) {
			this(model,new Input(null, messages),new Parameters("message", null, null, null, null, null, temperature, null, null));
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
			@JsonProperty("request_id") String request_id){
		
		public ChatCompletion(Output output,Usage usage,String request_id) {
			//this()
			this.output = output;
			this.usage = usage;
			this.request_id = request_id;
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Output(
			@JsonProperty("finish_reason")String finishReason,
			@JsonProperty("text") String text,
			@JsonProperty("choices") List<Choise> choise,
			@JsonProperty("task_id") String taskId,
			@JsonProperty("task_status") StatusStatus taskStatus,
			@JsonProperty("task_metrics") TaskMetrices taskMetrices,
			@JsonProperty("results") List<Results> results) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Choise(
			@JsonProperty("finish_reason")String finishReason,
			@JsonProperty("message") ChatCompletionMessage message) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Usage(
			@JsonProperty("total_tokens") Integer totalTokens,
			@JsonProperty("output_tokens")Integer outputTokens,
			@JsonProperty("input_tokens")Integer inputToken,
			@JsonProperty("image_count")Integer imageCount) {
	}
	
	@JsonInclude(Include.NON_NULL)
	public record QWenImageRequest(
			@JsonProperty("model") String model,
			@JsonProperty("input") Input input,
			@JsonProperty("parameters") Parameters parameters) {
		public QWenImageRequest(Input input,Parameters parameters) {
			this("wanx-v1",input,parameters);
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Results(
			@JsonProperty("url")String url,
			@JsonProperty("code")String code,
			@JsonProperty("message")String message) {
	}
	
	public record TaskMetrices(
			@JsonProperty("TOTAL")Integer total,
			@JsonProperty("SUCCEEDED")Integer succeeded,
			@JsonProperty("FAILED")Integer fauled) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record QWenImageResponse(
			@JsonProperty("request_id")String requestId,
			@JsonProperty("output") Output output) {
		
	}
	
	public enum StatusStatus{
		PENDING,RUNNING,SUCCEEDED,FAILED,UNKNOWN,
	}
	
	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest){
		Assert.notNull(chatRequest, "请求体不能为空。");
		logger.info("开始提交参数"+chatRequest.toString());
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
	
	public ResponseEntity<QWenImageResponse> createQwenImageTask(QWenImageRequest qwenImageRequest) {
		return this.restClient.post().uri("/api/v1/services/aigc/text2image/image-synthesis")
				.header("X-DashScope-Async", "enable")
				.body(qwenImageRequest).retrieve()
				.toEntity(QWenImageResponse.class);
	}
	
	public ResponseEntity<QWenImageResponse> findImageTaskResult(String taskId){
		return this.restClient.get().uri("/api/v1/tasks/{task_id}",taskId).retrieve().toEntity(QWenImageResponse.class);
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		String body = "{\"output\":{\"finish_reason\":\"stop\",\"text\":\"我是阿里云开发的一款超大规模语言模型，我叫通义千问。\"},\"usage\":{\"total_tokens\":20,\"output_tokens\":17,\"input_tokens\":3},\"request_id\":\"f978f627-fd0f-91fd-be5d-b3ec1ac394b1\"}";
		ObjectMapper objectMapper = new ObjectMapper();
		ChatCompletion chatCompletion = objectMapper.readValue(body,ChatCompletion.class);
		System.out.println(chatCompletion);
	}

}
