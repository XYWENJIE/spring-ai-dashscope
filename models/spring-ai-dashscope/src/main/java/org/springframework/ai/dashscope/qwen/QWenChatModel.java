package org.springframework.ai.dashscope.qwen;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatRequest;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatResponse;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.Output;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.Choices;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.Message;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.Role;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.FunctionTool;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.ChatCompletionFinishReason;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.ToolCall;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.Message.MediaContent;
import org.springframework.ai.dashscope.metadata.support.Model;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * 通义千问（QWen）的客户端实现类
 * @author 黄文杰
 */
public class QWenChatModel extends AbstractFunctionCallSupport<Message, QWenChatRequest, QWenChatResponse> implements ChatModel, StreamingChatModel {
	
	private final Logger logger = LoggerFactory.getLogger(QWenChatModel.class);
	
	private final QWenDashScopeService dashScopeService;
	
	private QWenChatOptions defaultChatOptions;
	
	private final RetryTemplate retryTemplate;

	private final Map<String,String> oldMessageMap = new HashMap<>();
	
	public QWenChatModel(QWenDashScopeService dashScopeService) {
		this(dashScopeService, QWenChatOptions.builder().withModel(Model.QWen_72B_CHAT).withTemperature(0.7f).build());
	}
	
	public QWenChatModel(QWenDashScopeService dashScopeService, QWenChatOptions options) {
		this(dashScopeService,options,null,RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}
	
	public QWenChatModel(QWenDashScopeService dashScopeService, QWenChatOptions options, FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
		super(functionCallbackContext);
		Assert.notNull(dashScopeService, "DashsCopeService must not be null");
		Assert.notNull(options, "Options must not be null");
		Assert.notNull(retryTemplate, "RetryTemplate must not be null");
		this.dashScopeService = dashScopeService;
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
		QWenChatRequest request = createRequest(prompt);
		return this.retryTemplate.execute(ctx -> {
			
			QWenChatResponse chatResponse = this.callWithFunctionSupport(request);

			if(chatResponse == null) {
				logger.warn("No chat completion returned for request:{}",chatResponse);
				return new ChatResponse(List.of());
			}
			//logger.info(chatResponse.toString());
			RateLimit rateLimit = null;
			List<Generation> generations = chatResponse.output().choices().stream().map(choices -> {
				return new Generation(choices.message().content(),toMap(chatResponse.requestId(),choices))
						.withGenerationMetadata(ChatGenerationMetadata.from(chatResponse.output().choices().get(0).finishReason().name(),null));
			}).toList();
			
			return new ChatResponse(generations,null);
		});
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return QWenChatOptions.fromOptions(this.defaultChatOptions);
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
		QWenChatRequest request = createRequest(prompt);
		return this.retryTemplate.execute(ctx -> {
			
			Flux<QWenChatResponse> completionChunks = this.dashScopeService.chatCompletionStream(request);
			ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();
			
			return completionChunks.map(this::chunkToChatCompletion).switchMap(cc-> handleFunctionCallOrReturnStream(request,Flux.just(cc)))
					.map(chatCompletion -> {
				try{
					chatCompletion = handleFunctionCallOrReturn(request,chatCompletion);

					String id = chatCompletion.requestId();
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

	private QWenChatResponse chunkToChatCompletion(QWenChatResponse chunk){
		String oldMessage = oldMessageMap.get(chunk.requestId());
		if(!StringUtils.hasLength(oldMessage)){
			oldMessage = "";
		}
		if(StringUtils.hasLength(chunk.code())){
			throw new IllegalStateException(chunk.message());
		}
		if(StringUtils.hasLength(chunk.output().choices().get(0).message().content())){
			String word;
			if(chunk.output().choices().get(0).message().content().charAt(0) == ':'){
				String sourceContent = chunk.output().choices().get(0).message().content().substring(1);
				word = sourceContent.replace(oldMessage, "");
				oldMessage = sourceContent;
			}else{
				word = chunk.output().choices().get(0).message().content().replace(oldMessage, "");
				oldMessage = chunk.output().choices().get(0).message().content();
			}
			oldMessageMap.put(chunk.requestId(),oldMessage);
			List<Choices> choicesList = chunk.output().choices().stream().map(choices -> {
				Message message = new Message(choices.message().role(),word);
				return new Choices(choices.finishReason(),message,choices.toolCall());
			}).toList();
			Output output = new Output(chunk.output().text(),chunk.output().finishReason(),choicesList);
			return new QWenChatResponse(output,chunk.usage(),chunk.requestId());
		}
		if(!chunk.output().choices().get(0).finishReason().equals(ChatCompletionFinishReason.NULL)){
			oldMessageMap.remove(chunk.requestId());
		}
		return new QWenChatResponse(chunk.output(),chunk.usage(),chunk.requestId());
	}
	
	public QWenChatRequest createRequest(Prompt prompt) {
		Set<String> functionsForThisRequest = new HashSet<>();
		List<Message> chatCompletionMessages = prompt.getInstructions().stream().map(m -> {
			if(!CollectionUtils.isEmpty(m.getMedia())){
				List<MediaContent> contents = new ArrayList<>(List.of(new MediaContent(null,m.getContent())));
				contents.addAll(m.getMedia().stream().map(media -> new MediaContent(media.getData().toString(),null)).toList());
				//TODO
				return new Message(Role.valueOf(m.getMessageType().name()),contents);
			}
			return new Message(Role.valueOf(m.getMessageType().name()),m.getContent());
		}).toList();

		QWenChatRequest request = new QWenChatRequest(this.defaultChatOptions.getModel(), new QWenDashScopeService.Input(chatCompletionMessages),new QWenDashScopeService.Parameters(this.defaultChatOptions.getTemperature()));
		
		if(prompt.getOptions() != null) {
			if(prompt.getOptions() instanceof ChatOptions runtimeOptions) {
				QWenChatOptions updatedRuntimeOptions =  ModelOptionsUtils.copyToTarget(runtimeOptions,
						ChatOptions.class, QWenChatOptions.class);
				
				Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions, IS_RUNTIME_CALL);
				
				functionsForThisRequest.addAll(promptEnabledFunctions);
				
				request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, QWenChatRequest.class);
				
			}else {
				throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
						+prompt.getOptions().getClass().getSimpleName());
			}
		}
		if(this.defaultChatOptions != null) {
			Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultChatOptions, IS_RUNTIME_CALL);
			
			functionsForThisRequest.addAll(defaultEnabledFunctions);
			
			request = ModelOptionsUtils.merge(request, this.defaultChatOptions, QWenChatRequest.class);
		}
		
		if(!CollectionUtils.isEmpty(functionsForThisRequest)) {
			request = ModelOptionsUtils.merge(QWenChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(), request, QWenChatRequest.class);
		}
		return request;
	}
	
	private List<FunctionTool> getFunctionTools(Set<String> functionName){
		return this.resolveFunctionCallbacks(functionName).stream().map(functionCallback -> {
			var function = new FunctionTool.Function(functionCallback.getName(),functionCallback.getDescription(),functionCallback.getInputTypeSchema());
			return new FunctionTool(function);
		}).toList();
	}

	@Override
	protected QWenChatRequest doCreateToolResponseRequest(QWenChatRequest previousRequest, Message responseMessage, List<Message> conversationHistory) {
		for(ToolCall toolCall : responseMessage.toolCalls()) {
			var functionName = toolCall.function().name();
			String functionArguments = toolCall.function().arguments();
			if(!this.functionCallbackRegister.containsKey(functionName)) {
				throw new IllegalStateException("No function callback found for function name: "+ functionName);
			}
			String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

			conversationHistory.add(new Message(Role.TOOL,functionResponse,functionName,null));
		}
		QWenDashScopeService.Input input = new QWenDashScopeService.Input(conversationHistory);
		QWenChatRequest newRequest = new QWenChatRequest(previousRequest.model(),input,null);
		newRequest = ModelOptionsUtils.merge(newRequest,previousRequest,QWenChatRequest.class);
		return newRequest;
	}

	//OK
	@Override
	protected List<Message> doGetUserMessages(QWenChatRequest request) {
		return request.input().messages();
	}

	@Override
	protected Message doGetToolResponseMessage(QWenChatResponse response) {
		return response.output().choices().iterator().next().message();
	}

	@Override
	protected QWenChatResponse doChatCompletion(QWenChatRequest request) {
		return this.dashScopeService.chatCompletion(request);
	}

	@Override
	protected Flux<QWenChatResponse> doChatCompletionStream(QWenChatRequest request) {
		return this.dashScopeService.chatCompletionStream(request).map(this::chunkToChatCompletion);
	}

	@Override
	protected boolean isToolFunctionCall(QWenChatResponse qWenChatResponse) {
		//logger.info("判断是否需要使用函数回调，返回参数：{}",body);
		if(qWenChatResponse == null) {
			return false;
		}
		var choices = qWenChatResponse.output().choices();
		if(CollectionUtils.isEmpty(choices)) {
			return false;
		}
		
		var choice = choices.get(0);
		return !CollectionUtils.isEmpty(choice.message().toolCalls()) && choice.finishReason() == ChatCompletionFinishReason.TOOL_CALLS;
	}

}
