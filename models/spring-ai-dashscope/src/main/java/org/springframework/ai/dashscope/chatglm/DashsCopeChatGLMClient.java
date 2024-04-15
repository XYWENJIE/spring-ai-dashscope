package org.springframework.ai.dashscope.chatglm;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.metadata.support.ChatModel;

public class DashsCopeChatGLMClient implements ChatClient {

    private final DashsCopeService dashsCopeService;

    public DashsCopeChatGLMClient(DashsCopeService dashsCopeService) {
        this.dashsCopeService = dashsCopeService;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        DashsCopeService.ChatCompletionRequest chatCompletionRequest = new DashsCopeService.ChatCompletionRequest(ChatModel.QWen_1_8B_CHAT, null,null);
        this.dashsCopeService.chatCompletionEntity(chatCompletionRequest);
        return null;
    }
}
