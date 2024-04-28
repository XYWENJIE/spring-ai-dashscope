package org.springframework.ai.dashscope.qwen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.image.ImageOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QWenImageOptions implements ImageOptions {

    @JsonIgnore
    private Integer n = 1;

    @JsonIgnore
    private String style;

    @JsonIgnore
    private String size = "1024*1024";

    @JsonIgnore
    private Integer seed;

    @JsonIgnore
    private Float refStrength;

    @JsonIgnore
    private String refModel;

    @JsonProperty("parameters")
    private DashsCopeService.Parameters parameters;


    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private final QWenImageOptions option;

        public Builder(){
            this.option = new QWenImageOptions();
        }

        public QWenImageOptions build(){
            this.option.buildParameters();
            return option;
        }
    }

    @Override
    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public String getModel() {
        return "wanx-v1";
    }

    @Override
    public Integer getWidth() {
        return 0;
    }

    @Override
    public Integer getHeight() {
        return 0;
    }

    @Override
    public String getResponseFormat() {
        return "";
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
        buildParameters();
    }

    public void buildParameters(){
        this.parameters =  new DashsCopeService.Parameters(style,size,n,seed,refStrength,refModel);
    }
}
