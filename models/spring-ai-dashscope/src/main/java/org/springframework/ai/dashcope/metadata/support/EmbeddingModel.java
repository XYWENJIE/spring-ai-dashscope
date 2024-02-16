package org.springframework.ai.dashcope.metadata.support;

public enum EmbeddingModel {
	TEXT_EMBEDDING_V1("text-embedding-v1"),TEXT_EMBEDDING_V2("text-embedding-v2");
	
	public String value;
	
	EmbeddingModel(String value) {
		this.value = value;
	}
	

}
