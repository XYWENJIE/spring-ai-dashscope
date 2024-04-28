package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DashscopeConnectionProperties.CONFIG_PREFIX)
public class DashscopeConnectionProperties extends DashscopeParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.dashscope";
}
