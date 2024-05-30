package org.springframework.ai.dashscope.api;

public abstract class ChatDashScopeService extends AbstractDashScopeService{

    public ChatDashScopeService(String accessToken, String requestUrl) {
        super(accessToken, requestUrl);
    }
}
