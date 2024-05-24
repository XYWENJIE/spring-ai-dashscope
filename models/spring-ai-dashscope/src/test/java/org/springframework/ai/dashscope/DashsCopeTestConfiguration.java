package org.springframework.ai.dashscope;

import org.springframework.ai.dashscope.qwen.QWenChatModel;
import org.springframework.ai.dashscope.qwen.QWenEmbeddingModel;
import org.springframework.ai.dashscope.qwen.QWenImageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.SpringBootConfiguration;

@SpringBootConfiguration
public class DashsCopeTestConfiguration {
	
	private String getApiKey() {
		String apiKey = System.getenv("DASHSCOPE_API_KEY");
		if(!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException("你必须提供一个API密钥。请将其放入名为DASHSCOPE_API_KEY的环境变量中。");
		}
		return apiKey;
	}
	
	@Bean
	public DashsCopeService dashCopeService() {
		return new DashsCopeService(getApiKey());
	}
	
	@Bean
	public QWenChatModel qWenChatClient(DashsCopeService dashCopeService) {
		return new QWenChatModel(dashCopeService);
	}
	
	@Bean
	public QWenImageModel qWenImageClient(DashsCopeService dashCopeService) {
		return new QWenImageModel(dashCopeService);
	}
	
	@Bean
	public EmbeddingModel qwenEmbeddingClient(DashsCopeService dashCopeService) {
		return new QWenEmbeddingModel(dashCopeService);
	}

}
