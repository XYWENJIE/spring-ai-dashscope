package org.springframework.ai.dashscope.metadata.support;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
public enum QWenImageModel {
    @JsonProperty("wanx-v1") WANX_V1,
    @JsonProperty("wanx-style-cosplay-v1") WANX_STYLE_COSPLAY_V1,
    @JsonProperty("wanx-style-repaint-v1") WANX_STYLE_REPAINT_V1,
    @JsonProperty("wanx-background-generation-v2") WANX_BACKGROUND_GENERATION_V2,
    @JsonProperty("wanx-anytext-v1") WANX_ANYTEXT_V1,
    @JsonProperty("wanx-sketch-to-image-lite") WANX_SKETCH_TO_IMAGE_LITE,

}
