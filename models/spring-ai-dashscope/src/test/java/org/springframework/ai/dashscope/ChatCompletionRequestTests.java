package org.springframework.ai.dashscope;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.ai.dashscope.qwen.QWenChatOptions;

public class ChatCompletionRequestTests {
	
	@Test
	public void promptOptionsTools() {
		final String TOOL_FUNCTION_NAME = "CurrentWeather";
		
		var client = new QWenChatClient(new DashsCopeService(""),QWenChatOptions.builder().withModel(null).build());
		
		var request = client.createRequest(new Prompt("test message content"));
	}

}
