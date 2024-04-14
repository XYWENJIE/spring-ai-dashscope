package org.springframework.ai.dashscope.qwen;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletion;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionFinishReason;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage.MediaContent;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage.Role;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionRequest;
import org.springframework.ai.dashscope.DashsCopeService.Choise;
import org.springframework.ai.dashscope.DashsCopeService.ToolCall;
import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import reactor.core.publisher.Flux;

/**
 * 通义千问（QWen）的客户端实现类
 * @author 黄文杰
 */
public class QWenChatClient extends AbstractFunctionCallSupport<ChatCompletionMessage, ChatCompletionRequest, ResponseEntity<ChatCompletion>> implements ChatClient,StreamingChatClient {
	
	private final Logger logger = LoggerFactory.getLogger(QWenChatClient.class);
	
	private final DashsCopeService dashCopeService;
	
	private QWenChatOptions defaultChatOptions;
	
	private final RetryTemplate retryTemplate;
	
	public QWenChatClient(DashsCopeService dashCopeService) {
		this(dashCopeService, QWenChatOptions.builder().withModel(ChatModel.QWen_72B_CHAT).withTemperature(0.7f).build());
	}
	
	public QWenChatClient(DashsCopeService dashCopeService, QWenChatOptions options) {
		this(dashCopeService,options,null,RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}
	
	public QWenChatClient(DashsCopeService dashsCopeService,QWenChatOptions options,FunctionCallbackContext functionCallbackContext,RetryTemplate retryTemplate) {
		super(functionCallbackContext);
		Assert.notNull(dashsCopeService, "DashsCopeService must not be null");
		Assert.notNull(options, "Options must not be null");
		Assert.notNull(retryTemplate, "RetryTemplate must not be null");
		this.dashCopeService = dashsCopeService;
		this.defaultChatOptions = options;
		this.retryTemplate = retryTemplate;
	}
	
	/**
	 * Spring AI升级中的历史遗留问题
	 */
//	private final RetryTemplate retryTemplate = RetryTemplate.builder()
//			.maxAttempts(10)
//			.retryOn(IOException.class)
//			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
//			.build();

	@Override
	public ChatResponse call(Prompt prompt) {
		return this.retryTemplate.execute(ctx -> {
			
			ResponseEntity<DashsCopeService.ChatCompletion> completionEntity = this.dashCopeService.chatCompletionEntity(createRequest(prompt));
			
			var chatCompletion = completionEntity.getBody();
			if(chatCompletion == null) {
				logger.warn("No chat completion returned for request:{}",chatCompletion);
				return new ChatResponse(List.of());
			}
			//TODO
			RateLimit rateLimit = null;
			List<Generation> generations = chatCompletion.output().choise().stream().map(choise -> {
				return new Generation(choise.message().content(),toMap(chatCompletion.request_id(),choise))
						.withGenerationMetadata(ChatGenerationMetadata.from(chatCompletion.output().choise().get(0).finishReason().name(),null));
			}).toList();
			
			return new ChatResponse(generations,null);
		});
	}
	
	private Map<String,Object> toMap(String id,Choise choice){
		Map<String,Object> map = new HashMap<>();
		return map;
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		return this.retryTemplate.execute(ctx -> {
			
			Flux<DashsCopeService.ChatCompletion> completionChunks = this.dashCopeService.chatCompletionStream(createRequest(prompt));
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
	
	DashsCopeService.ChatCompletionRequest createRequest(Prompt prompt) {
		List<Message> messages = prompt.getInstructions();
		List<ChatCompletionMessage> chatCompletionMessages = messages.stream()
				.map(m -> {
					List<MediaContent> contents = new ArrayList<>(List.of(new MediaContent(m.getContent())));
					if(!CollectionUtils.isEmpty(m.getMedia())) {
						contents.addAll(m.getMedia().stream().map(media -> new MediaContent(null)).toList());
					}
					return new ChatCompletionMessage(contents,ChatCompletionMessage.Role.valueOf(m.getMessageType().name()));
				}).toList();
		
		DashsCopeService.ChatCompletionRequest request  = new DashsCopeService.ChatCompletionRequest(chatCompletionMessages, this.defaultChatOptions.getModel(), this.defaultChatOptions.getTemperature());
		return request;
	}
	//TODO 函数回调

	@Override
	protected ChatCompletionRequest doCreateToolResponseRequest(ChatCompletionRequest previousRequest,
			ChatCompletionMessage responseMessage, List<ChatCompletionMessage> conversationHistory) {
		for(ToolCall toolCall : responseMessage.toolCalls()) {
			var functionName = toolCall.function().name();
			String functionArguments = toolCall.function().arguments();
			if(!this.functionCallbackRegister.containsKey(functionName)) {
				throw new IllegalStateException("No function callback found for function name: "+ functionName);
			}
			String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);
			
			conversationHistory.add(new ChatCompletionMessage(functionResponse, Role.TOOL,functionName,"call_abc123",null));
		}
		return null;
	}

	//OK
	@Override
	protected List<ChatCompletionMessage> doGetUserMessages(ChatCompletionRequest request) {
		return request.input().messages();
	}

	@Override
	protected ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<ChatCompletion> response) {
		return response.getBody().output().choise().iterator().next().message();
	}

	@Override
	protected ResponseEntity<ChatCompletion> doChatCompletion(ChatCompletionRequest request) {
		return this.dashCopeService.chatCompletionEntity(request);
	}

	@Override
	protected boolean isToolFunctionCall(ResponseEntity<ChatCompletion> chatCompletion) {
		var body = chatCompletion.getBody();
		if(body == null) {
			return false;
		}
		var choices = body.output().choise();
		if(CollectionUtils.isEmpty(choices)) {
			return false;
		}
		
		var choice = choices.get(0);
		return !CollectionUtils.isEmpty(choice.message().toolCalls()) && choice.finishReason() == ChatCompletionFinishReason.TOOL_CALLS;
	}

}
