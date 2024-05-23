package org.springframework.ai.dashscope.qwen;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.image.*;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * 阿里云的通义万相
 * 相关文档地址：https://help.aliyun.com/zh/dashscope/developer-reference/api-details-9?spm=a2c4g.11186623.0.0.2859602dP7PUx7#25745d61fbx49
 * @author 黄文杰
 */
public class QWenImageClient implements ImageModel {
	
	private final Logger logger = LoggerFactory.getLogger(QWenImageClient.class);
	
	private final DashsCopeService dashsCopeService;
	private final QWenImageOptions defaultOptions;
	
//	private final RetryTemplate retryTemplate = RetryTemplate.builder()
//			.maxAttempts(10)
//			.retryOn(DashsCopeService.DashCopeApiException.class)
//			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
//			.build();

	private final RetryTemplate retryTemplate;
	
	public QWenImageClient(DashsCopeService dashCopeService) {
		this(dashCopeService, QWenImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public QWenImageClient(DashsCopeService dashsCopeService, QWenImageOptions defaultOptions, RetryTemplate retryTemplate){
		Assert.notNull(dashsCopeService, "dashCopeService 不能为空");
		this.dashsCopeService = dashsCopeService;
		this.defaultOptions =defaultOptions;
		this.retryTemplate = retryTemplate;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		DashsCopeService.QWenImageResponse taskImageResponse = this.retryTemplate.execute(ctx -> {
			//ImageOptions imageOptions = imagePrompt.getOptions();
			String instructions = imagePrompt.getInstructions().get(0).getText();
			
			DashsCopeService.QWenImageRequest imageRequest = new DashsCopeService.QWenImageRequest(new DashsCopeService.QWenImageRequest.Input(instructions,null,null), null);
			if(this.defaultOptions != null){
				imageRequest = ModelOptionsUtils.merge(this.defaultOptions,imageRequest,DashsCopeService.QWenImageRequest.class);
			}
			ResponseEntity<DashsCopeService.QWenImageResponse> responseEntity = this.dashsCopeService.createQwenImageTask(imageRequest);
			return responseEntity.getBody();
		});
		try {
			if(taskImageResponse.output().taskStatus() == DashsCopeService.StatusStatus.PENDING) {
				logger.info("任务提交成功，需要排队,休眠4秒");
				Thread.sleep(Duration.ofSeconds(4).toMillis());
				logger.info("休眠完成，查询任务结果！");
			}
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return this.retryTemplate.execute(ctx -> {
			logger.info("请求查询结果");
			DashsCopeService.QWenImageResponse resultImageResponse;
			DashsCopeService.StatusStatus queryTaskStatus = taskImageResponse.output().taskStatus();
			do{
				ResponseEntity<DashsCopeService.QWenImageResponse> responseEntity = this.dashsCopeService.findImageTaskResult(taskImageResponse.output().taskId());
				resultImageResponse = responseEntity.getBody();
				queryTaskStatus = resultImageResponse.output().taskStatus();
				logger.info("查询结果完成");
				if(resultImageResponse.output().taskStatus() != DashsCopeService.StatusStatus.SUCCEEDED) {
					DashsCopeService.TaskMetrices taskMetrices = resultImageResponse.output().taskMetrices();
					logger.info("任务还在进行生成中，一共有{}任务，完成{}个任务，请稍后",taskMetrices.total(),taskMetrices.succeeded());
					try {
						Thread.sleep(Duration.ofSeconds(3).toMillis());
					}catch (Exception e){
						logger.error(e.getMessage(),e);
						throw new DashsCopeService.DashCopeApiException(e.getMessage());
					}

				}
			}while (queryTaskStatus == DashsCopeService.StatusStatus.PENDING || queryTaskStatus == DashsCopeService.StatusStatus.RUNNING);
			return convertResponse(resultImageResponse);
		});
	}
	
	private ImageResponse convertResponse(DashsCopeService.QWenImageResponse qwenImageResponse) {
		if(qwenImageResponse == null) {
			logger.warn("No image response returned for request: {}",qwenImageResponse);
			return new ImageResponse(List.of());
		}
		
		List<ImageGeneration> imageGenerationList = qwenImageResponse.output().results().stream().map(entry -> {
			return new ImageGeneration(new Image(entry.url(),null));
		}).toList();
		return new ImageResponse(imageGenerationList);
	}

}
