package org.springframework.ai.dashscope.qwen.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.dashscope.api.AbstractDashScopeService;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatRequest;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService.QWenChatResponse;

public class QWenDashScopeService extends AbstractDashScopeService<QWenChatRequest, QWenChatResponse,Object> {

    public QWenDashScopeService(String accessToken){
        super(accessToken,"/services/aigc/text-generation/generation");
    }

    @JsonInclude(Include.NON_NULL)
    public record QWenChatRequest(@JsonProperty("model") String model,@JsonProperty("input") Input input){}

    @JsonInclude(Include.NON_NULL)
    public record Input(){}

    public record QWenChatResponse(){}

    @Override
    protected QWenChatResponse chatCompletion(QWenChatRequest request) {
        return internalInvocation(request,QWenChatResponse.class);
    }
}
