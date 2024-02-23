package org.springframework.ai.autoconfigure.dashscope.qwen;

import org.springframework.ai.dashscope.metadata.support.ChatModel;
import org.springframework.ai.dashscope.qwen.QWenChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(DashsCopeQwenChatProperties.CONFIG_PREFIX)
public class DashsCopeQwenChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.dashscope.qwen.chat";

    private String model = ChatModel.QWen_72B_CHAT.getModelValue();

    @NestedConfigurationProperty
    private QWenChatOptions options = QWenChatOptions.builder().build();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public QWenChatOptions getOptions() {
        return options;
    }

    public void setOptions(QWenChatOptions options) {
        this.options = options;
    }
}
