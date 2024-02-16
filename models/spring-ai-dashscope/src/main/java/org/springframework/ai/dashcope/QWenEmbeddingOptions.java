package org.springframework.ai.dashcope;

import org.springframework.ai.dashcope.metadata.support.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class QWenEmbeddingOptions implements EmbeddingOptions{
	
	private @JsonProperty("model") EmbeddingModel model;
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		
		private QWenEmbeddingOptions embeddingOptions;
		
		public Builder() {
			this.embeddingOptions = new QWenEmbeddingOptions();
		}
		
		public Builder withModel(EmbeddingModel model) {
			this.embeddingOptions.model = model;
			return this;
		}
		
		public QWenEmbeddingOptions build() {
			return this.embeddingOptions;
		}
	}

}
