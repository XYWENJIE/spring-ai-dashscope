package org.springframework.ai.dashscope.qwen;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.api.ImageDashScopeService;
import org.springframework.ai.dashscope.api.ImageDashScopeService.Input;
import org.springframework.ai.dashscope.api.ImageDashScopeService.TaskStatus;
import org.springframework.ai.dashscope.api.ImageDashScopeService.TaskMetrics;
import org.springframework.ai.image.*;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * 阿里云的通义万相
 * 相关文档地址：<a href="https://help.aliyun.com/zh/dashscope/developer-reference/api-details-9?spm=a2c4g.11186623.0.0.2859602dP7PUx7#25745d61fbx49">...</a>
 * @author 黄文杰
 */
public class QWenImageModel implements ImageModel {
	
	private final Logger logger = LoggerFactory.getLogger(QWenImageModel.class);
	
	private final ImageDashScopeService dashsCopeService;
	private final QWenImageOptions defaultOptions;

	private final RetryTemplate retryTemplate;
	
	public QWenImageModel(ImageDashScopeService dashCopeService) {
		this(dashCopeService, QWenImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public QWenImageModel(ImageDashScopeService dashsCopeService, QWenImageOptions defaultOptions, RetryTemplate retryTemplate){
		Assert.notNull(dashsCopeService, "dashCopeService 不能为空");
		this.dashsCopeService = dashsCopeService;
		this.defaultOptions =defaultOptions;
		this.retryTemplate = retryTemplate;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		ImageDashScopeService.ImageResponse taskImageResponse = this.retryTemplate.execute(ctx -> {
			String instructions = imagePrompt.getInstructions().get(0).getText();
			
			ImageDashScopeService.ImageRequest imageRequest = new ImageDashScopeService.ImageRequest(new Input(instructions,null,null), null);
			if(this.defaultOptions != null){
				imageRequest = ModelOptionsUtils.merge(this.defaultOptions,imageRequest,ImageDashScopeService.ImageRequest.class);
			}
			ImageDashScopeService.ImageResponse imageResponse = this.dashsCopeService.submitTask(imageRequest);
			logger.info(imageResponse.toString());
			return imageResponse;
		});
		try {
			if(taskImageResponse.output().taskStatus() == TaskStatus.PENDING) {
				Thread.sleep(Duration.ofSeconds(3).toMillis());
			}
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return this.retryTemplate.execute(ctx -> {
			ImageDashScopeService.ImageResponse resultImageResponse;
			TaskStatus queryTaskStatus;
			do{
				resultImageResponse = this.dashsCopeService.queryTask(taskImageResponse.output().taskId());
				queryTaskStatus = resultImageResponse.output().taskStatus();
				logger.info("查询结果完成,状态{}",queryTaskStatus);
				if(queryTaskStatus == TaskStatus.FAILED){
					logger.error(resultImageResponse.toString());
					throw new DashsCopeService.DashCopeApiException(resultImageResponse.output().message());
				}
				if(resultImageResponse.output().taskStatus() != TaskStatus.SUCCEEDED) {
					TaskMetrics taskMetrics = resultImageResponse.output().taskMetrics();
					logger.info("任务还在进行生成中，一共有{}任务，完成{}个任务，请稍后，3秒后重新查询",taskMetrics.total(),taskMetrics.succeeded());
					try {
						Thread.sleep(Duration.ofSeconds(3).toMillis());
					}catch (Exception e){
						logger.error(e.getMessage(),e);
						throw new DashsCopeService.DashCopeApiException(e.getMessage());
					}

				}
			}while (queryTaskStatus == TaskStatus.PENDING || queryTaskStatus == TaskStatus.RUNNING);
			return convertResponse(resultImageResponse);
		});
	}
	
	private ImageResponse convertResponse(ImageDashScopeService.ImageResponse imageResponse) {
		if(imageResponse == null) {
			logger.warn("No image response returned for");
			return new ImageResponse(List.of());
		}
		
		List<ImageGeneration> imageGenerationList = imageResponse.output().results().stream().map(entry -> new ImageGeneration(new Image(entry.url(),null))).toList();
		return new ImageResponse(imageGenerationList);
	}

}
