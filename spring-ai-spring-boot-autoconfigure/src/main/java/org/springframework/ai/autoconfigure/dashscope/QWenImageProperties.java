package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.ai.dashscope.qwen.QWenImageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(QWenImageProperties.CONFIG_PREFIX)
public class QWenImageProperties extends DashscopeParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.qwen.image";

    private boolean enabled = true;

    @NestedConfigurationProperty
    private QWenImageOptions options;

    public QWenImageOptions getOptions() {
        return options;
    }

    public void setOptions(QWenImageOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
