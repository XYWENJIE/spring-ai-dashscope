package org.springframework.ai.autoconfigure.dashscope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY",matches = ".*")
public class DashscopeAutoConfigurationIT {

    private static final Log logger = LogFactory.getLog(DashscopeAutoConfigurationIT.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.ai.dashscope.apikey="+System.getenv("DASHSCOPE_API_KEY"))
            .withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class, DashscopeAutoConfiguration.class));

    @Test
    void generate(){
        contextRunner.run(context -> {
            QWenChatClient chatClient = context.getBean(QWenChatClient.class);
            String response = chatClient.call("Hello");
            assertThat(response).isNotEmpty();
            logger.info("Response:"+response);
        });
    }
}
