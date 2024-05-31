package org.springframework.ai.dashscope.qwen.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.dashscope.api.AbstractDashScopeService;
import org.springframework.ai.dashscope.metadata.support.Model;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatRequest;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QWenDashScopeService extends AbstractDashScopeService<QWenChatRequest, QWenChatResponse,Object> {

    private static String QWEN_CHAT_VL_REQUEST_URL = "/services/aigc/multimodal-generation/generation";

    private static String QWEN_CHAT_REQUEST_URL = "/services/aigc/text-generation/generation";

    public QWenDashScopeService(String accessToken){
        super(accessToken,QWEN_CHAT_REQUEST_URL);
    }

    @JsonInclude(Include.NON_NULL)
    public record QWenChatRequest(@JsonProperty("model") Model model, @JsonProperty("input") Input input,
                                  @JsonProperty("parameters") Parameters parameters){}

    @JsonInclude(Include.NON_NULL)
    public record Input(@JsonProperty("messages") List<Message> messages ){


    }

    @JsonInclude(Include.NON_NULL)
    public record Message(@JsonProperty("role") Role role, @JsonProperty("content") Object rowContent,
                          @JsonProperty("name") String name, @JsonProperty("tool_calls") List<ToolCall> toolCalls){
        public Message(Role role,Object content){
            this(role,content,null,null);
        }

        public Message(Role role,List<MediaContent> content){
            this(role,content,null,null);
        }

        @JsonInclude(Include.NON_NULL)
        public record MediaContent(@JsonProperty("image") String image,@JsonProperty("text") String text){}

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
                                   @JsonProperty("request_id") String requestId,@JsonProperty("code") String code,
                                   @JsonProperty("message") String message){
        
        public QWenChatResponse(Output output, Usage usage, String requestId){
            this(output,usage,requestId,null,null);
        }
    }



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

    @JsonInclude(Include.NON_NULL)
    public record ToolCall(@JsonProperty("type") String type,@JsonProperty("function") Function function,@JsonProperty("id") String id){


        public record Function(@JsonProperty("name") String name,@JsonProperty("arguments") String arguments){ }
    }

    @JsonInclude(Include.NON_NULL)
    public record Usage(@JsonProperty("output_tokens")Integer outputTokens,@JsonProperty("input_tokens") Integer inputTokens,
                        @JsonProperty("total_tokens") Integer totalTokens){}

    @Override
    public QWenChatResponse chatCompletion(QWenChatRequest request) {
        if(request.model.equals(Model.QWen_VL_MAX) || request.model.equals(Model.QWen_VL_PLUS)){
            this.requestUrl = QWEN_CHAT_VL_REQUEST_URL;
        }else{
            this.requestUrl = QWEN_CHAT_REQUEST_URL;
        }
        return internalInvocation(request,QWenChatResponse.class);
    }

    @Override
    public Flux<QWenChatResponse> chatCompletionStream(QWenChatRequest request){
        if(request.model.equals(Model.QWen_VL_MAX) || request.model.equals(Model.QWen_VL_PLUS)){
            this.requestUrl = QWEN_CHAT_VL_REQUEST_URL;
        }else{
            this.requestUrl = QWEN_CHAT_REQUEST_URL;
        }
        return this.internalInvocationStream(request,QWenChatResponse.class);
    }
}
