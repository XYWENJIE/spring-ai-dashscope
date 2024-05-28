package org.springframework.ai.dashscope.qwen.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.api.AbstractDashScopeService;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatRequest;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import reactor.core.publisher.Flux;

import javax.tools.Tool;
import java.util.List;
import java.util.Map;

public class QWenDashScopeService extends AbstractDashScopeService<QWenChatRequest, QWenChatResponse,Object> {

    public QWenDashScopeService(String accessToken){
        super(accessToken,"/services/aigc/text-generation/generation");
    }

    @JsonInclude(Include.NON_NULL)
    public record QWenChatRequest(@JsonProperty("model") String model,@JsonProperty("input") Input input,
                                  @JsonProperty("parameters") Parameters parameters){}

    @JsonInclude(Include.NON_NULL)
    public record Input(@JsonProperty("messages") List<Message> messages ){


    }

    public record Message(@JsonProperty("role") Role role, @JsonProperty("content") String content,
                          @JsonProperty("name") String name, @JsonProperty("tool_calls") List<ToolCall> toolCalls){
        public Message(Role role,String content){
            this(role,content,null,null);
        }
    }

    public enum Role {
        @JsonProperty("system") SYSTEM,
        @JsonProperty("user") USER,
        @JsonProperty("assistant") ASSISTANT,
        @JsonProperty("tool") TOOL
    }

    @JsonInclude(Include.NON_NULL)
    public record Parameters(@JsonProperty("result_format") String resultFormat,@JsonProperty("seed") Integer seed,
                             @JsonProperty("max_tokens") Integer maxTokens,@JsonProperty("top_p") Float topP,
                             @JsonProperty("top_k") Float topK,@JsonProperty("repetition_penalty") Float repetitionPenalty,
                             @JsonProperty("presence_penalty") Float presencePenalty,@JsonProperty("temperature") Float temperature,
                             @JsonProperty("stop") String[] stop,@JsonProperty("enable_search") Boolean enableSearch,
                             @JsonProperty("incremental_output") Boolean incrementalOutput,@JsonProperty("tools") List<FunctionTool> tools){
        public Parameters(Integer seed,Integer maxTokens,Float topP,Float topK,Float repetitionPenalty,
                          Float presencePenalty,Float temperature,String[] stop,Boolean enableSearch,Boolean incrementalOutput,
                          List<FunctionTool> tools){
            this("message",seed,maxTokens,topP,topK,repetitionPenalty,presencePenalty,temperature,stop,enableSearch,incrementalOutput,tools);
        }

        public Parameters(Float temperature){
            this("message",null,null,null,null,null,null,temperature,null,null,null,null);
        }

        public Parameters(List<FunctionTool> functionTools){
            this("message",null,null,null,null,null,null,null,null,null,null,functionTools);
        }
    }

    @JsonInclude(Include.NON_NULL)
    public record FunctionTool(
            @JsonProperty("type") FunctionTool.Type type,
            @JsonProperty("function") FunctionTool.Function function ) {

        public FunctionTool(FunctionTool.Function function) {
            this(FunctionTool.Type.FUNCTION,function);
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

    @JsonInclude(Include.NON_NULL)
    public record QWenChatResponse(@JsonProperty("output")Output output,@JsonProperty("usage") Usage usage,
                                   @JsonProperty("request_id") String requestId){}

    @JsonInclude(Include.NON_NULL)
    public record Output(@JsonProperty("text") String text,@JsonProperty("finish_reason") String finishReason,
                         @JsonProperty("choices") List<Choices> choices){}

    @JsonInclude(Include.NON_NULL)
    public record Choices(
            @JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
            @JsonProperty("message") Message message,
            @JsonProperty("tool_calls") ToolCall toolCall) {
    }

    public enum ChatCompletionFinishReason{
        @JsonProperty("tool_calls") TOOL_CALLS,
        @JsonProperty("null") NULL,
        @JsonProperty("stop") STOP
    }

    public record ToolCall(@JsonProperty("type") String type,@JsonProperty("function") Function function){
        public record Function(@JsonProperty("name") String name,@JsonProperty("arguments") String arguments){

        }
    }

    @JsonInclude(Include.NON_NULL)
    public record Usage(@JsonProperty("output_tokens")Integer outputTokens,@JsonProperty("input_tokens") Integer inputTokens,
                        @JsonProperty("total_tokens") Integer totalTokens){}

    @Override
    public QWenChatResponse chatCompletion(QWenChatRequest request) {
        return internalInvocation(request,QWenChatResponse.class);
    }

    @Override
    public Flux<QWenChatResponse> chatCompletionStream(QWenChatRequest request){
        return this.internalInvocationStream(request,QWenChatResponse.class);
    }
}
