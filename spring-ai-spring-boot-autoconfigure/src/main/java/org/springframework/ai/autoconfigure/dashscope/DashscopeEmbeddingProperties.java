package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DashscopeEmbeddingProperties.CONFIG_PREFIX)
public class DashscopeEmbeddingProperties extends DashscopeParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.embedding";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
