package org.springframework.ai.autoconfigure.dashscope;

/**
 * 阿里云公用属性
 * @author 黄文杰
 * @since 0.8.1
 */
public class DashscopeParentProperties {

    private String apikey;

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}
