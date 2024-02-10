package org.springframework.ai.dashcope;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashcope.DashCopeService.ChatCompletion;
import org.springframework.ai.dashcope.DashCopeService.ChatCompletionMessage;
import org.springframework.ai.dashcope.DashCopeService.ChatCompletionRequest;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;

import reactor.core.publisher.Flux;

/**
 * 该类是访问
 */
public class QWenChatClient implements ChatClient,StreamingChatClient {
	
	private final Logger logger = LoggerFactory.getLogger(QWenChatClient.class);
	
	private String model = "qwen-72b-chat";
	
	private Double temperature = 0.7;
	
	private final DashCopeService dashCopeService;
	
	private QWenChatOptions defaultChatOptions;
	
	public QWenChatClient(DashCopeService dashCopeService) {
		this.dashCopeService = dashCopeService;
		this.defaultChatOptions = new QWenChatOptions();
	}
	
	private final RetryTemplate retryTemplate = RetryTemplate.builder()
			.maxAttempts(10)
			.retryOn(IOException.class)
			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
			.build();

	@Override
	public ChatResponse call(Prompt prompt) {
		return this.retryTemplate.execute(ctx -> {
			
			ResponseEntity<ChatCompletion> completionEntity = this.dashCopeService.chatCompletionEntity(createRequest(prompt));
			
			var chatCompletion = completionEntity.getBody();
			if(chatCompletion == null) {
				logger.warn("No chat completion returned for request:{}",chatCompletion);
				return new ChatResponse(List.of());
			}
			logger.info("QwenChatClient返回处理信息");
			//TODO
			RateLimit rateLimit = null;
			List<Generation> generations = chatCompletion.output().choise().stream().map(choice -> {
				return new Generation(choice.message().content(),Map.of("role",choice.message().role())).withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason(),null));
			}).toList();
			
			return new ChatResponse(generations,null);
		});
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		return this.retryTemplate.execute(ctx -> {
			
			Flux<ChatCompletion> completionChunks = this.dashCopeService.chatCompletionStream(createRequest(prompt));
			ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap();
			
			return completionChunks.map(chunk -> {
				String chunkId = chunk.request_id();
//				List<Generation> generations = chunk.output().choise().stream().map(choise -> {
//					if(choise.message().role() != null) {
//						roleMap.putIfAbsent(chunkId, choise.message().role());
//					}
//					var generation = new Generation(choise.message().content(),Map.of("role",roleMap.get(chunkId)));
//					if(choise.finishReason() != null) {
//						generation = generation.withGenerationMetadata(ChatGenerationMetadata.from(choise.finishReason(), null));
//					}
//					return generation;
//				}).toList();
				return new ChatResponse(null);
			});
		});
	}
	
	private ChatCompletionRequest createRequest(Prompt prompt) {
		List<Message> messages = prompt.getInstructions();
		List<ChatCompletionMessage> chatCompletionMessages = messages.stream()
				.map(m -> new ChatCompletionMessage(m.getMessageType().name(),
						m.getContent()))
				.toList();
		
		ChatCompletionRequest request  = new ChatCompletionRequest(chatCompletionMessages, this.defaultChatOptions.getModel(), this.defaultChatOptions.getTemperature());
		return request;
	}

}
