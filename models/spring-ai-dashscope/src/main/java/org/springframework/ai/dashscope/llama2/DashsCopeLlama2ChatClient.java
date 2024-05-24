package org.springframework.ai.dashscope.llama2;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.llama2.api.Llama2ChatDashCopeApi.Llama2ChatRequest;

/**
 * DashCope的Llama2客户端实现
 * Llama 2系列是来自Meta开发并公开发布的大型语言模型（LLMs）。该系列模型提供了多种参数大小（7B、13B和70B等）的版本，
 * 并同时提供了预训练和针对对话场景的微调版本。 Llama 2系列使用了2T token进行训练，相比于LLama多出40%，上下文长度从LLama的2048升级到4096，
 * 可以理解更长的文本， 在多个公开基准测试上超过了已有的开源模型。 采用了高质量的数据进行微调和基于人工反馈的强化学习训练，具有较高的可靠性和安全性。
 * @author 黄文杰
 */
public class DashsCopeLlama2ChatClient implements ChatModel {

	@Override
	public ChatResponse call(Prompt prompt) {
		var request = createRequest(prompt);
		return null;
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return null;
	}

	Llama2ChatRequest createRequest(Prompt prompt) {
		//final String proptValue = prompt.get
		return null;
	}

}
