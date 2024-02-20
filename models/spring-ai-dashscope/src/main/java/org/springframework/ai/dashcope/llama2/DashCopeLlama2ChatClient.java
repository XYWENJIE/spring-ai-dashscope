package org.springframework.ai.dashcope.llama2;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashcope.llama2.api.Llama2ChatDashCopeApi.Llama2ChatRequest;

/**
 * DashCope的Llama2
 * @author Benjamin
 */
public class DashCopeLlama2ChatClient implements ChatClient{

	@Override
	public ChatResponse call(Prompt prompt) {
		var request = createRequest(prompt);
		return null;
	}
	
	Llama2ChatRequest createRequest(Prompt prompt) {
		//final String proptValue = prompt.get
		return null;
	}

}
