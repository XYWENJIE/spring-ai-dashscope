package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.dashscope.qwen.QWenChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(DashscopeProperties.CONFIG_PREFIX)
public class DashscopeProperties extends DashscopeParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.qwen.chat";

    private static final ChatModel DEFAULT_CHAT_MODEL = ChatModel.QWen_TURBO;

    private static final Double DEFAULT_TEMPERATURE = 0.7;

    private boolean enabled = true;

    @NestedConfigurationProperty
    private QWenChatOptions options = QWenChatOptions.builder()
            .withModel(DEFAULT_CHAT_MODEL)
            .withTemperature(DEFAULT_TEMPERATURE.floatValue())
            .build();

    public QWenChatOptions getOptions(){
        return options;
    }

    public void setOptions(QWenChatOptions options){
        this.options = options;
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
}
