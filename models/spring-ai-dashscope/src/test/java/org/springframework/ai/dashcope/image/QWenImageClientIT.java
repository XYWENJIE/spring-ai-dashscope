package org.springframework.ai.dashcope.image;

import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QWenImageClientIT {
	
	@Autowired
	private final ImageClient qwenImageClient;
	
	@Test
	public void imageAsUrlTest() {
		var options = ImageOptionsBuilder.builder().withHeight(1024).withWidth(1024).build();
		var instructions = """
				A light cream colored mini golden doodle with a sign that contains the message "I'm on my way to BARCADE!".
				""";
		
		ImagePrompt imagePrompt = new ImagePrompt(instructions, options);
		
		ImageResponse imageResponse = qwenImageClient.call(imagePrompt);
	}

}
