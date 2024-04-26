package org.springframework.ai.dashscope;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
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
import reactor.core.publisher.Mono;

public class DashsCopeService {
	
	private final Logger logger = LoggerFactory.getLogger(DashsCopeService.class);

	private final String baseUrl = "https://dashscope.aliyuncs.com";

	private final RestClient restClient;
	
	private final WebClient webClient;
	
	private final ObjectMapper objectMapper;

	public DashsCopeService(String accessToken) {
		
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
							objectMapper.readValue(response.getBody(), ResponseError.class)));
				}
			}
		};
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeaders(jsonContentHeaders)
				.requestInterceptor(new LogHttpRequestInterceptor())
				.defaultStatusHandler(responseErrorHandler)
				.build();
		this.webClient = WebClient.builder().baseUrl(baseUrl)
				.defaultHeaders(jsonContentHeaders)
				.defaultHeaders(httpHeaders -> {
			httpHeaders.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
			httpHeaders.set("X-DashScope-SSE","enable");
		}).build();
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
		
		private final Logger logger = LoggerFactory.getLogger(DashsCopeService.class);

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
//			if(httpResponse.getBody() != null) {
//				try(InputStream responseBodyStream = httpResponse.getBody()){
//					String responseBody = new String(StreamUtils.copyToByteArray(responseBodyStream),StandardCharsets.UTF_8);
//					logger.info("Response Body : {}",responseBody);
//					ChatCompletion chatCompletion = objectMapper.readValue(responseBody, ChatCompletion.class);
//					logger.info(chatCompletion.toString());
//				}
//			}
			return httpResponse;
		}
	}

	/**
	 * ResponseError 类是一个记录类（Java 14+ 新特性），用于封装错误响应信息。
	 * 它通过 Jackson 序列化注解来控制 JSON 序列化的行为，确保只有非空字段被包含在 JSON 中。
	 *
	 * @param code 错误代码，对应错误的唯一标识。
	 * @param message 错误消息，描述错误的详细信息。
	 * @param requestId 请求标识，用于追踪和记录请求。
	 */
	@JsonInclude(Include.NON_NULL)
	public record ResponseError(
			@JsonProperty("code")String code,
			@JsonProperty("message") String message,
			@JsonProperty("request_id")String requestId) {}



	/**
	 * ChatCompletionRequest 类是一个记录类，用于表示聊天完成请求的数据结构。
	 * 它包含以下字段：
	 * @param model 指定用于聊天完成的模型名称。
	 * @param input 表示聊天的输入内容，包含具体的对话信息。
	 * @param parameters 包含聊天完成请求的额外参数，可选。
	 */
	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
			@JsonProperty("model") ChatModel model,
			@JsonProperty("input") Input input,
			@JsonProperty("parameters") Parameters parameters) {
		
		public ChatCompletionRequest(List<ChatCompletionMessage> messages,ChatModel model,Float temperature) {
			this(model,new Input(null, messages,null),new Parameters("message", null, null, null, null, null, temperature, null, null,null,null));
		}
		
		public ChatCompletionRequest(Input input,ChatModel model,Parameters parameters) {
			this(model, input, parameters);
		}

		@JsonInclude(Include.NON_NULL)
		public record Input(
				@JsonProperty("prompt") String prompt,
				@JsonProperty("messages") List<ChatCompletionMessage> messages,
				@JsonProperty("texts")List<String> texts) {

		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatGLMRequest(){

	}

	public record CharGLMResponse(){

	}
	
	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
			@JsonProperty("content")Object rowContent,
			@JsonProperty("role") Role role,
			@JsonProperty("name") String name,
			@JsonProperty("tool_call_id") String toolCallId,
			@JsonProperty("tool_calls") List<ToolCall> toolCalls) {
		
		public ChatCompletionMessage(Object rowContent,Role role) {
			this(rowContent, role, null,null,null);
		}
		
		public enum Role {
			@JsonProperty("system") SYSTEM,
			@JsonProperty("user") USER,
			@JsonProperty("assistant") ASSISTANT,
			@JsonProperty("tool") TOOL
		}
		
		@JsonInclude(Include.NON_NULL)
		public record MediaContent(
				@JsonProperty("text") String text,
				@JsonProperty("image") String image) {


		}

		public String content(){
			if(this.rowContent== null){
				return null;
			}
			if(this.rowContent instanceof String text){
				return text;
			}
			if(this.rowContent instanceof List<?> list){
				HashMap<String,String> content = (HashMap<String,String>)list.get(0);
				return content.get("text");
			}
			throw new IllegalArgumentException("The content is not a string!");
		}
		
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
			@JsonProperty("incremental_output")Boolean incrementalOutput,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("text_type")String textType) {	
		
		public Parameters(String textType) {
			this(null,null,null,null,null,null,null,null, true,null, textType);
		}
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
			@JsonProperty("choices") List<Choices> choices,
			@JsonProperty("task_id") String taskId,
			@JsonProperty("task_status") StatusStatus taskStatus,
			@JsonProperty("task_metrics") TaskMetrices taskMetrices,
			@JsonProperty("results") List<Results> results,
			@JsonProperty("embeddings") List<Embedding> embeddings) {
		
	}
	
	public record Embedding(
			@JsonProperty("text_index")Integer textIndex,
			@JsonProperty("embedding") List<Double> embedding) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Choices(
			@JsonProperty("finish_reason")ChatCompletionFinishReason finishReason,
			@JsonProperty("message") ChatCompletionMessage message,
			@JsonProperty("tool_calls") ToolCall toolCall) {
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public record Usage(
			@JsonProperty("total_tokens") Integer totalTokens,
			@JsonProperty("output_tokens")Integer outputTokens,
			@JsonProperty("input_tokens")Integer inputToken,
			@JsonProperty("image_count")Integer imageCount) {
	}
	
	@JsonInclude(Include.NON_NULL)
	public record FunctionTool(
			@JsonProperty("type") Type type,
			@JsonProperty("function") Function function ) {
		
		public FunctionTool(Function function) {
			this(Type.FUNCTION,function);
		}
		
		public enum Type{
			@JsonProperty("function") FUNCTION
		}
		
		@JsonInclude(Include.NON_NULL)
		public record Function(
				@JsonProperty("name") String name,
				@JsonProperty("description") String description,
				@JsonProperty("parameters") Map<String,Object> parameters) {

			@ConstructorBinding
			public Function(String name,String description,String jsonSchema) {
				this(name, description, ModelOptionsUtils.jsonToMap(jsonSchema));
			}

		}


	}
	
	
	
	public record FunctionParameters(
			@JsonProperty("type") String type,
			@JsonProperty("properties") Properties properties,
			@JsonProperty("required") List<String> required) {}
	
	public record Properties(
			@JsonProperty("location") Location location,
			@JsonProperty("unit") Unit unit) {}
	
	public record Location(
			@JsonProperty("type")String type,
			@JsonProperty("description") String description) {}
	
	public record Unit(
			@JsonProperty("type") String type,
			@JsonProperty("enum") List<String> enums) {}
	
	public record ToolCall(
			@JsonProperty("function") ChatCompletionFunction function,
			@JsonProperty("type") String type) {}
	
	public record ChatCompletionFunction(
			@JsonProperty("name")String name,
			@JsonProperty("arguments") String arguments) {}
	
	@JsonInclude(Include.NON_NULL)
	public record QWenImageRequest(
			@JsonProperty("model") String model,
			@JsonProperty("input") Input input,
			@JsonProperty("parameters") Parameters parameters) {
		public QWenImageRequest(Input input,Parameters parameters) {
			this("wanx-v1",input,parameters);
		}

		@JsonInclude(Include.NON_NULL)
		public record Input(
				@JsonProperty("prompt") String prompt,
				@JsonProperty("messages") List<ChatCompletionMessage> messages,
				@JsonProperty("texts")List<String> texts) {

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
	
	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest(
			String model,Input input,Parameters parameters) {
		
		public EmbeddingRequest(String model,List<String> texts,String textType) {
			this(model, new Input(null, null,texts), new Parameters(textType));
		}

		@JsonInclude(Include.NON_NULL)
		public record Input(
				@JsonProperty("prompt") String prompt,
				@JsonProperty("messages") List<ChatCompletionMessage> messages,
				@JsonProperty("texts")List<String> texts) {

		}
	}
	
	public record EmbeddingResponse(
			String requestId,Usage usage,Output output) {
		
	}
	
	public enum StatusStatus{
		PENDING,RUNNING,SUCCEEDED,FAILED,UNKNOWN,
	}
	
	public enum ChatCompletionFinishReason{
		@JsonProperty("null") NULL,
		@JsonProperty("stop") STOP,
		@JsonProperty("tool_calls") TOOL_CALLS
	}
	
	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest){
		Assert.notNull(chatRequest, "请求体不能为空。");
        logger.info("开始提交参数{}", chatRequest);
		String uri = getModelSpecificURI(chatRequest.model);
		ResponseEntity<String> jsonResponse =  this.restClient.post()
				.uri(uri)
				.body(chatRequest).retrieve().toEntity(String.class);
		String jsonBody = jsonResponse.getBody();
		logger.info("返回JSON:{}",jsonBody);
        try {
            return new ResponseEntity<>(objectMapper.readValue(jsonBody, ChatCompletion.class), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
	
	public Flux<ChatCompletion> chatCompletionStream(ChatCompletionRequest chatRequest){
		Assert.notNull(chatRequest, "请求体不能为空。");
		logger.info("提交参数{}",chatRequest);
		try{
			logger.info(objectMapper.writeValueAsString(chatRequest));
		}catch (Exception e){
			logger.error(e.getMessage());
		}
		// 添加图文识别 测试，Qwen对这方面调用比OpenAI负责
		String uri = getModelSpecificURI(chatRequest.model);
		return this.webClient.post()
				.uri(uri)
				.body(Mono.just(chatRequest),ChatCompletionRequest.class).retrieve().bodyToFlux(ChatCompletion.class);
	}

	public String getModelSpecificURI(ChatModel model){
		switch (model){
			case QWen_VL_PLUS:
			case QWen_VL_MAX:
				return "/api/v1/services/aigc/multimodal-generation/generation";
			default:
				return "/api/v1/services/aigc/text-generation/generation";
		}
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
	
	public ResponseEntity<EmbeddingResponse> embeddingRequest(EmbeddingRequest embeddingRequest) {
		return this.restClient.post()
				.uri("/api/v1/services/embeddings/text-embedding/text-embedding")
				.body(embeddingRequest).retrieve()
				.toEntity(EmbeddingResponse.class);
	}

	public ResponseEntity<CharGLMResponse> chatGLMEntity(ChatGLMRequest chatGLMRequest){
		return this.restClient.post().uri("").body(chatGLMRequest).retrieve().toEntity(CharGLMResponse.class);
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		String body = "{\"output\":{\"finish_reason\":\"stop\",\"text\":\"我是阿里云开发的一款超大规模语言模型，我叫通义千问。\"},\"usage\":{\"total_tokens\":20,\"output_tokens\":17,\"input_tokens\":3},\"request_id\":\"f978f627-fd0f-91fd-be5d-b3ec1ac394b1\"}";
		ObjectMapper objectMapper = new ObjectMapper();
		ChatCompletion chatCompletion = objectMapper.readValue(body,ChatCompletion.class);
		System.out.println(chatCompletion);
	}

}
