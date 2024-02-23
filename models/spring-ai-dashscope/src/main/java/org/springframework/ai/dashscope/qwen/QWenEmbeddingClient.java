package org.springframework.ai.dashscope.qwen;

import static org.assertj.core.api.Assertions.contentOf;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.metadata.support.EmbeddingModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * 通用文本向量，是通义实验室基于LLM底座的多语言文本统一向量模型，面向全球多个主流语种，提供高水准的向量服务，帮助开发者将文本数据快速转换为高质量的向量数据。
 * @author 黄文杰
 */
public class QWenEmbeddingClient extends AbstractEmbeddingClient {
	
	private static final Logger logger = LoggerFactory.getLogger(QWenEmbeddingClient.class);
	
	public static final EmbeddingModel DEFAULT_OPENAI_EMBEDDING_MODEL = EmbeddingModel.TEXT_EMBEDDING_V1;
	
	private final RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(10)
			.retryOn(DashsCopeService.DashCopeApiException.class)
			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3* 60000))
			.withListener(new RetryListener() {
				public <T extends Object, E extends Throwable> void onError(org.springframework.retry.RetryContext context, org.springframework.retry.RetryCallback<T,E> callback, Throwable throwable) {
					logger.warn("Retry error. Retry count:"+context.getRetryCount(),throwable);
				};
			}).build();
	
	private final QWenEmbeddingOptions embeddingOptions;
	
	private final DashsCopeService dashCopeService;
	
	private final MetadataMode metadataMode;
	
	public QWenEmbeddingClient(DashsCopeService dashCopeService) {
		this(dashCopeService,MetadataMode.EMBED);
	}
	
	public QWenEmbeddingClient(DashsCopeService dashCopeService, MetadataMode metadataMode) {
		this(dashCopeService,metadataMode,QWenEmbeddingOptions.builder().withModel(DEFAULT_OPENAI_EMBEDDING_MODEL).build());
		//this(null,null,null);
	}
	
	public QWenEmbeddingClient(DashsCopeService dashCopeService, MetadataMode metadataMode, QWenEmbeddingOptions embeddingOptions) {
		Assert.notNull(dashCopeService, "dashCopeService不能为空");
		Assert.notNull(metadataMode, "metadataMode不能为空");
		Assert.notNull(embeddingOptions, "embeddingOptions不能为空");
		this.dashCopeService = dashCopeService;
		this.metadataMode = metadataMode;
		this.embeddingOptions = embeddingOptions;
	}

	@Override
	public List<Double> embed(Document document) {
		Assert.notNull(document, "Document 不能为空！");
		return this.embed(document.getFormattedContent(this.metadataMode));
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {
		return this.retryTemplate.execute(ctx -> {
			DashsCopeService.EmbeddingRequest embeddingRequest = new DashsCopeService.EmbeddingRequest(this.embeddingOptions.getModel().value, request.getInstructions(), "document");
			DashsCopeService.EmbeddingResponse embeddingResponse = this.dashCopeService.embeddingRequest(embeddingRequest).getBody();
			if(embeddingResponse == null) {
				logger.warn("No {}",embeddingRequest);
			}
			var metadata = generateResponseMetadata(embeddingRequest.model(),embeddingResponse.usage());
			List<Embedding> embeddings = embeddingResponse.output().embeddings().stream().map(e-> new Embedding(e.embedding(),e.textIndex())).toList();
			return new EmbeddingResponse(embeddings,metadata);
		});
	}
	
	private EmbeddingResponseMetadata generateResponseMetadata(String model, DashsCopeService.Usage usage) {
		EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
		metadata.put("model", model);
		metadata.put("total-tokens", usage.totalTokens());
		return metadata;
	}

}
