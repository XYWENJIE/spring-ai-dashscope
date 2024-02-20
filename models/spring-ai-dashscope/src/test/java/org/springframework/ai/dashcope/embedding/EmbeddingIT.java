package org.springframework.ai.dashcope.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.dashcope.qwen.QWenEmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmbeddingIT {
	
	@Autowired
	private QWenEmbeddingClient embeddingClient;
	
	@Test
	public void simpleEmbedding() {
		assertThat(embeddingClient).isNotNull();
		
		EmbeddingResponse embeddingResponse = embeddingClient.embedForResponse(List.of("Hello word"));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		assertThat(embeddingResponse.getResults().get(0)).isNotNull();
		//assertThat(embeddingResponse.getMetadata()).containsEntry("model", "");
	}

}
