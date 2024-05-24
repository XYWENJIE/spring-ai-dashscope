package org.springframework.ai.dashscope.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.qwen.QWenEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmbeddingIT {
	
	private final Logger logger = LoggerFactory.getLogger(EmbeddingIT.class);
	
	@Autowired
	private QWenEmbeddingModel embeddingClient;
	
	@Test
	public void simpleEmbedding() {
		assertThat(embeddingClient).isNotNull();
		
		EmbeddingResponse embeddingResponse = embeddingClient.embedForResponse(List.of("Hello word"));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		logger.info("{}",embeddingResponse.getResult().getOutput());
		assertThat(embeddingResponse.getResults().get(0)).isNotNull();
		//assertThat(embeddingResponse.getMetadata()).containsEntry("model", "");
	}

}
