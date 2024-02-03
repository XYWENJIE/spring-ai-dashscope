package org.springframework.ai.dashcope;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashcope.DashCopeService.DashCopeApiException;
import org.springframework.ai.dashcope.DashCopeService.QWenImageRequest;
import org.springframework.ai.dashcope.DashCopeService.Input;
import org.springframework.ai.dashcope.DashCopeService.QWenImageResponse;
import org.springframework.ai.dashcope.DashCopeService.StatusStatus;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * 阿里云的通义万相
 * @author 黄文杰
 */
public class QWenImageClient implements ImageClient {
	
	private final Logger logger = LoggerFactory.getLogger(QWenImageClient.class);
	
	private final DashCopeService dashCopeService;
	
	private final RetryTemplate retryTemplate = RetryTemplate.builder()
			.maxAttempts(10)
			.retryOn(DashCopeApiException.class)
			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
			.build();
	
	public QWenImageClient(DashCopeService dashCopeService) {
		Assert.notNull(dashCopeService, "dashCopeService 不能为空");
		this.dashCopeService = dashCopeService;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		QWenImageResponse taskImageResponse = this.retryTemplate.execute(ctx -> {
			ImageOptions imageOptions = imagePrompt.getOptions();
			
			String instructions = imagePrompt.getInstructions().get(0).getText();
			
			QWenImageRequest imageRequest = new QWenImageRequest(new Input(instructions,null), null);
			ResponseEntity<QWenImageResponse> responseEntity = this.dashCopeService.createQwenImageTask(imageRequest);
			return responseEntity.getBody();
		});
		try {
			if(taskImageResponse.output().taskStatus() == StatusStatus.PENDING) {
				logger.info("任务提交成功，需要排队,休眠3秒");
				Thread.sleep(3000);
				logger.info("休眠完成，查询任务结果！");
			}
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return this.retryTemplate.execute(ctx -> {
			ResponseEntity<QWenImageResponse> responseEntity = this.dashCopeService.findImageTaskResult(taskImageResponse.output().taskId());
			QWenImageResponse resultImageResponse = responseEntity.getBody();
			if(resultImageResponse.output().taskStatus() != StatusStatus.SUCCEEDED) {
				logger.info("任务还在进行生成中，请稍后");
				throw new DashCopeApiException("任务还在进行生成请稍后");
			}
			return convertResponse(resultImageResponse);
		});
	}
	
	private ImageResponse convertResponse(QWenImageResponse qwenImageResponse) {
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
