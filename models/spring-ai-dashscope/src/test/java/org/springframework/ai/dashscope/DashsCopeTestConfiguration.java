package org.springframework.ai.dashscope;

import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.ai.dashscope.qwen.QWenEmbeddingClient;
import org.springframework.ai.dashscope.qwen.QWenImageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.boot.SpringBootConfiguration;

@SpringBootConfiguration
public class DashsCopeTestConfiguration {
	
	private String getApiKey() {
		System.out.println(System.getenv());
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
	public QWenChatClient qWenChatClient(DashsCopeService dashCopeService) {
		return new QWenChatClient(dashCopeService);
	}
	
	@Bean
	public QWenImageClient qWenImageClient(DashsCopeService dashCopeService) {
		return new QWenImageClient(dashCopeService);
	}
	
	@Bean
	public EmbeddingClient qwenEmbeddingClient(DashsCopeService dashCopeService) {
		return new QWenEmbeddingClient(dashCopeService);
	}

}
