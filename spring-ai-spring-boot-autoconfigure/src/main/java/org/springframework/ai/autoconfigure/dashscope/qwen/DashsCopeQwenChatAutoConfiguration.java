package org.springframework.ai.autoconfigure.dashscope.qwen;

import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({DashsCopeQwenChatProperties.class})
public class DashsCopeQwenChatAutoConfiguration {

    @Bean
    public QWenChatClient qWenChatClient(DashsCopeService dashsCopeService){
        return new QWenChatClient(dashsCopeService);
    }
}
