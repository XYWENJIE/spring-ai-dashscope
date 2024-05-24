package org.springframework.ai.dashscope.chatglm;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.metadata.support.Model;

public class DashsCopeChatGLMClient implements ChatModel {

    private final DashsCopeService dashsCopeService;

    public DashsCopeChatGLMClient(DashsCopeService dashsCopeService) {
        this.dashsCopeService = dashsCopeService;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        DashsCopeService.ChatCompletionRequest chatCompletionRequest = new DashsCopeService.ChatCompletionRequest(false, Model.QWen_1_8B_CHAT, null,null);
        this.dashsCopeService.chatCompletionEntity(chatCompletionRequest);
        return null;
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }
}
