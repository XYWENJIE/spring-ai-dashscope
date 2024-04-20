package org.springframework.ai.dashscope.qwen;

import java.util.*;
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
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletion;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionFinishReason;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage.MediaContent;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionMessage.Role;
import org.springframework.ai.dashscope.DashsCopeService.ChatCompletionRequest;
import org.springframework.ai.dashscope.DashsCopeService.Choices;
import org.springframework.ai.dashscope.DashsCopeService.FunctionTool;
import org.springframework.ai.dashscope.DashsCopeService.ToolCall;
import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.model.ModelOptionsUtils;
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
		ChatCompletionRequest request = createRequest(prompt);
		return this.retryTemplate.execute(ctx -> {
			
			ResponseEntity<DashsCopeService.ChatCompletion> completionEntity = this.callWithFunctionSupport(request);
			
			var chatCompletion = completionEntity.getBody();

			if(chatCompletion == null) {
				logger.warn("No chat completion returned for request:{}",chatCompletion);
				return new ChatResponse(List.of());
			}
			logger.info(chatCompletion.toString());
			//TODO
			RateLimit rateLimit = null;
			List<Generation> generations = chatCompletion.output().choices().stream().map(choices -> {
				return new Generation(choices.message().content(),toMap(chatCompletion.request_id(),choices))
						.withGenerationMetadata(ChatGenerationMetadata.from(chatCompletion.output().choices().get(0).finishReason().name(),null));
			}).toList();
			
			return new ChatResponse(generations,null);
		});
	}
	
	private Map<String,Object> toMap(String id,Choices choice){
		Map<String,Object> map = new HashMap<>();

		var message = choice.message();
		if(message.role() != null){
			map.put("role",message.role().name());
		}
		if(choice.finishReason() != null){
			map.put("finishReason",choice.finishReason().name());
		}
		map.put("id",id);
		return map;
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		ChatCompletionRequest request = createRequest(prompt);
		return this.retryTemplate.execute(ctx -> {
			
			Flux<DashsCopeService.ChatCompletion> completionChunks = this.dashCopeService.chatCompletionStream(request);
			ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap();
			
			return completionChunks.map(chatCompletion -> {
				try{
					chatCompletion = handleFunctionCallOrReturn(request,ResponseEntity.of(Optional.of(chatCompletion)))
							.getBody();

					String id = chatCompletion.request_id();
					List<Generation> generations = chatCompletion.output().choices().stream().map(choices -> {
						if(choices.message().role() != null){
							roleMap.putIfAbsent(id,choices.message().role().name());
						}
						String finish = (choices.finishReason() != null ? choices.finishReason().name() : "");
						var generation = new Generation(choices.message().content(),Map.of("id",id,"role",roleMap.get(id),"finishReason",finish));
						if(choices.finishReason() != null){
							generation = generation.withGenerationMetadata(ChatGenerationMetadata.from(choices.finishReason().name(),null));
						}
						return generation;
					}).toList();

					return new ChatResponse(generations);

				}catch (Exception e) {
					logger.error("Error processing chat completion",e);
					return new ChatResponse(List.of());
				}
			});
		});
	}
	
	public ChatCompletionRequest createRequest(Prompt prompt) {
		Set<String> functionsForThisRequest = new HashSet<>();
		List<ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map(m -> {
			if(!CollectionUtils.isEmpty(m.getMedia())){
				List<MediaContent> contents = new ArrayList<>(List.of(new MediaContent(m.getContent(),null)));
				contents.addAll(m.getMedia().stream().map(media -> new MediaContent(null,media.getData().toString())).toList());
				return new ChatCompletionMessage(contents,ChatCompletionMessage.Role.valueOf(m.getMessageType().name()));
			}
			return new ChatCompletionMessage(m.getContent(),ChatCompletionMessage.Role.valueOf(m.getMessageType().name()));
		}).toList();

		ChatCompletionRequest request = new ChatCompletionRequest(chatCompletionMessages, this.defaultChatOptions.getModel(), this.defaultChatOptions.getTemperature());
		
		if(prompt.getOptions() != null) {
			if(prompt.getOptions() instanceof ChatOptions runtimeOptions) {
				QWenChatOptions updatedRuntimeOptions =  ModelOptionsUtils.copyToTarget(runtimeOptions,
						ChatOptions.class, QWenChatOptions.class);
				
				Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions, IS_RUNTIME_CALL);
				
				functionsForThisRequest.addAll(promptEnabledFunctions);
				
				request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, ChatCompletionRequest.class);
				
			}else {
				throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
						+prompt.getOptions().getClass().getSimpleName());
			}
		}
		if(this.defaultChatOptions != null) {
			Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultChatOptions, !IS_RUNTIME_CALL);
			
			functionsForThisRequest.addAll(defaultEnabledFunctions);
			
			request = ModelOptionsUtils.merge(request, this.defaultChatOptions, ChatCompletionRequest.class);
		}
		
		if(!CollectionUtils.isEmpty(functionsForThisRequest)) {
			request = ModelOptionsUtils.merge(QWenChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(), request, ChatCompletionRequest.class);
		}
		return request;
	}
	
	private List<FunctionTool> getFunctionTools(Set<String> functionName){
		return this.resolveFunctionCallbacks(functionName).stream().map(functionCallback -> {
			var function = new FunctionTool.Function(functionCallback.getName(),functionCallback.getDescription(),functionCallback.getInputTypeSchema());
			return new FunctionTool(function);
		}).toList();
	}
	
	
	//TODO 函数回调
	@Override
	protected ChatCompletionRequest doCreateToolResponseRequest(ChatCompletionRequest previousRequest,
			ChatCompletionMessage responseMessage, List<ChatCompletionMessage> conversationHistory) {
		logger.info("doCreateToolResponseRequest");
		for(ToolCall toolCall : responseMessage.toolCalls()) {
			var functionName = toolCall.function().name();
			String functionArguments = toolCall.function().arguments();
			if(!this.functionCallbackRegister.containsKey(functionName)) {
				throw new IllegalStateException("No function callback found for function name: "+ functionName);
			}
			String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);
			
			conversationHistory.add(new ChatCompletionMessage(functionResponse, Role.TOOL,functionName,"call_abc123",null));
		}

		ChatCompletionRequest newRequest = new ChatCompletionRequest(conversationHistory,null,null);
		newRequest = ModelOptionsUtils.merge(newRequest,previousRequest,ChatCompletionRequest.class);
		return newRequest;
	}

	//OK
	@Override
	protected List<ChatCompletionMessage> doGetUserMessages(ChatCompletionRequest request) {
		logger.info("doGetUserMessages");
		return request.input().messages();
	}

	@Override
	protected ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<ChatCompletion> response) {
		logger.info("doGetToolResponseMessage");
		return response.getBody().output().choices().iterator().next().message();
	}

	@Override
	protected ResponseEntity<ChatCompletion> doChatCompletion(ChatCompletionRequest request) {

		return this.dashCopeService.chatCompletionEntity(request);
	}

	@Override
	protected boolean isToolFunctionCall(ResponseEntity<ChatCompletion> chatCompletion) {
		logger.info("isToolFunctionCall");
		var body = chatCompletion.getBody();
		if(body == null) {
			return false;
		}
		logger.info("查看参数：{}",body);
		var choices = body.output().choices();
		if(CollectionUtils.isEmpty(choices)) {
			return false;
		}
		
		var choice = choices.get(0);
		return !CollectionUtils.isEmpty(choice.message().toolCalls()) && choice.finishReason() == ChatCompletionFinishReason.TOOL_CALLS;
	}

}
