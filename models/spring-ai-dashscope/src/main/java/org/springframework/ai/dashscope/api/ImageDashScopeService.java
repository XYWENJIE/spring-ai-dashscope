package org.springframework.ai.dashscope.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.dashscope.api.ImageDashScopeService.ImageRequest;
import org.springframework.ai.dashscope.api.ImageDashScopeService.ImageResponse;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDashScopeService extends AbstractDashScopeService<ImageRequest,ImageResponse,Object>{

    private static final Logger logger = LoggerFactory.getLogger(ImageDashScopeService.class);

    private Map<String, ImageResponse> taskResponse = new HashMap<>();

    public ImageDashScopeService(String accessToken) {
        super(accessToken, "/services/aigc/text2image/image-synthesis");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ImageRequest(@JsonProperty("model") ImageModel model, @JsonProperty("input") Input input,
                                   @JsonProperty("parameters") Parameters parameters){

        public ImageRequest(Input input, Parameters parameters){
            this(ImageModel.WANX_V1,input,parameters);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Input(@JsonProperty("prompt") String prompt,@JsonProperty("negative_prompt") String negativePrompt,
                        @JsonProperty("ref_img") String refImg){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Parameters(@JsonProperty("style") String style,@JsonProperty("size") String size,@JsonProperty("n") Integer n,
                             @JsonProperty("seed") Integer seed,@JsonProperty("ref_strength") Float refStrength,
                             @JsonProperty("ref_mode") String refMode){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ImageResponse(@JsonProperty("request_id") String requestId, @JsonProperty("output") Output output,
                                    @JsonProperty("usage") Usage usage, @JsonProperty("code") String code,
                                @JsonProperty("message") String message){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Output(@JsonProperty("task_id") String taskId, @JsonProperty("task_status") TaskStatus taskStatus,
                         @JsonProperty("task_metrics") TaskMetrics taskMetrics, @JsonProperty("results") List<Result> results,
                         @JsonProperty("message") String message, @JsonProperty("code") String code){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Usage(@JsonProperty("image_count") Integer imageCount){}

    public record TaskMetrics(
            @JsonProperty("TOTAL")Integer total,
            @JsonProperty("SUCCEEDED")Integer succeeded,
            @JsonProperty("FAILED")Integer fauled) {
    }

    public record Result(@JsonProperty("url") String url,@JsonProperty("code") String code,
                         @JsonProperty("message") String message){}

    public enum TaskStatus{
        PENDING,SUCCEEDED,RUNNING,FAILED
    }

    public enum ImageModel {
        @JsonProperty("wanx-v1") WANX_V1,
        @JsonProperty("wanx-style-cosplay-v1") WANX_STYLE_COSPLAY_V1,
        @JsonProperty("wanx-style-repaint-v1") WANX_STYLE_REPAINT_V1,
        @JsonProperty("wanx-background-generation-v2") WANX_BACKGROUND_GENERATION_V2,
        @JsonProperty("wanx-anytext-v1") WANX_ANYTEXT_V1,
        @JsonProperty("wanx-sketch-to-image-lite") WANX_SKETCH_TO_IMAGE_LITE,

    }

    @Override
    public ImageResponse submitTask(ImageRequest request) {
        ImageResponse imageResponse = this.internalInvocation(request, ImageResponse.class);
        //this.taskResponse.put(qWenImageResponse.requestId,qWenImageResponse);
        return imageResponse;
    }

    @Override
    public ImageResponse queryTask(@NotNull String taskId) {
        logger.info("查询任务：{}",taskId);
        return this.restClient.get().uri("/tasks/{taskId}",taskId).retrieve().body(ImageResponse.class);
    }

    @Override
    protected void addHttpHeaders(HttpHeaders httpHeaders) {
        httpHeaders.set("X-DashScope-Async","enable");
    }
}
