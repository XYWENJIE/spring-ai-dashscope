package org.springframework.ai.dashcope;

import java.util.List;

import org.springframework.ai.dashcope.metadata.support.EmbeddingModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.util.Assert;

/**
 * 通义千问的的文本内嵌客户端实现
 * @author 黄文杰
 */
public class QWenEmbeddingClient extends AbstractEmbeddingClient {
	
	public static final EmbeddingModel DEFAULT_OPENAI_EMBEDDING_MODEL = EmbeddingModel.TEXT_EMBEDDING_V1;
	
	private final QWenEmbeddingOptions embeddingOptions;
	
	private final DashCopeService dashCopeService;
	
	private final MetadataMode metadataMode;
	
	public QWenEmbeddingClient(DashCopeService dashCopeService) {
		this(dashCopeService,MetadataMode.EMBED);
	}
	
	public QWenEmbeddingClient(DashCopeService dashCopeService,MetadataMode metadataMode) {
		this(dashCopeService,metadataMode,QWenEmbeddingOptions.builder().withModel(DEFAULT_OPENAI_EMBEDDING_MODEL).build());
		//this(null,null,null);
	}
	
	public QWenEmbeddingClient(DashCopeService dashCopeService,MetadataMode metadataMode,QWenEmbeddingOptions embeddingOptions) {
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
		// TODO Auto-generated method stub
		return null;
	}

}
