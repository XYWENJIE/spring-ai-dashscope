package org.springframework.ai.dashscope.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.DashsCopeTestConfiguration;
import org.springframework.ai.image.*;
import org.springframework.ai.image.ImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {DashsCopeTestConfiguration.class})
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY", matches = ".+")
public class QWenImageClientIT {
	
	private final Logger logger = LoggerFactory.getLogger(QWenImageClientIT.class);
	
	@Autowired
	private ImageModel qwenImageClient;
	
	@Test
	public void imageAsUrlTest() {
		ImageOptions options = ImageOptionsBuilder.builder().withHeight(1024).withWidth(1024).build();
		String instructions = """
				A light cream colored mini golden doodle with a sign that contains the message "I'm on my way to BARCADE!".
				""";
		
		ImagePrompt imagePrompt = new ImagePrompt(instructions, options);
		
		ImageResponse imageResponse = qwenImageClient.call(imagePrompt);
		imageResponse.getResults().forEach(result -> {
			logger.info("URL:{}",result.getOutput().getUrl());
		});
	}

}
