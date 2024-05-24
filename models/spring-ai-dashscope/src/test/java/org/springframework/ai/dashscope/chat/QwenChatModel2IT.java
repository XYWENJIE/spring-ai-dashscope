package org.springframework.ai.dashscope.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.qwen.QWenChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(classes = QwenChatModel2IT.Config.class)
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY",matches = ".+")
public class QwenChatModel2IT {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private QWenChatModel qWenChatClient;
	
	@Test
	void responseFormatTest() {}
	
	static class Config {
		
		@Bean
		public DashsCopeService dashsCopeService() {
			return new DashsCopeService(System.getenv("DASHSCOPE_API_KEY"));
		} 
		
		@Bean
		public QWenChatModel qWenChatClient(DashsCopeService dashsCopeService) {
			return new QWenChatModel(dashsCopeService);
		}
	}
	
	

}
