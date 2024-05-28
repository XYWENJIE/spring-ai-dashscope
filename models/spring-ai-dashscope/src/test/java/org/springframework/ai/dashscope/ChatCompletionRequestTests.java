package org.springframework.ai.dashscope;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.qwen.QWenChatModel;
import org.springframework.ai.dashscope.qwen.QWenChatOptions;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService;

public class ChatCompletionRequestTests {
	
	@Test
	public void promptOptionsTools() {
		final String TOOL_FUNCTION_NAME = "CurrentWeather";
		
		var client = new QWenChatModel(new QWenDashScopeService(""),QWenChatOptions.builder().withModel(null).build());
		
		var request = client.createRequest(new Prompt("test message content"));
	}

}
