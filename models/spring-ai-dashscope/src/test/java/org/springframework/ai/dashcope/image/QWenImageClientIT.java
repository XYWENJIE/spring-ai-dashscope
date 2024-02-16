package org.springframework.ai.dashcope.image;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashcope.DashCopeTestConfiguration;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {DashCopeTestConfiguration.class})
public class QWenImageClientIT {
	
	private final Logger logger = LoggerFactory.getLogger(QWenImageClientIT.class);
	
	@Autowired
	private ImageClient qwenImageClient;
	
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
