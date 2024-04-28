package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.ai.dashscope.qwen.QWenImageOption;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(QWenImageProperties.CONFIG_PREFIX)
public class QWenImageProperties extends DashscopeParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.qwen.image";

    private boolean enabled = true;

    @NestedConfigurationProperty
    private QWenImageOption option;

    public QWenImageOption getOption() {
        return option;
    }

    public void setOption(QWenImageOption option) {
        this.option = option;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
