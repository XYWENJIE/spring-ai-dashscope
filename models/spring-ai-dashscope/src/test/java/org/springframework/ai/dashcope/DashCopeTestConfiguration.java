package org.springframework.ai.dashcope;

import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.boot.SpringBootConfiguration;

@SpringBootConfiguration
public class DashCopeTestConfiguration {
	
	private String getApiKey() {
		String apiKey = System.getenv("DASHSCOPE_API_KEY");
		if(!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException("你必须提供一个API密钥。请将其放入名为DASHSCOPE_API_KEY的环境变量中。");
		}
		return apiKey;
	}
	
	@Bean
	public DashCopeService dashCopeService() {
		return new DashCopeService(getApiKey());
	}
	
	@Bean
	public QWenChatClient qWenChatClient(DashCopeService dashCopeService) {
		return new QWenChatClient(dashCopeService);
	}
	
	@Bean
	public QWenImageClient qWenImageClient(DashCopeService dashCopeService) {
		return new QWenImageClient(dashCopeService);
	}

}
