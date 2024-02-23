package org.springframework.ai.autoconfigure.dashscope;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DashsCopeAliyunConnectionProperties.CONFIG_PREFIX)
public class DashsCopeAliyunConnectionProperties {

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.aliyun";

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
