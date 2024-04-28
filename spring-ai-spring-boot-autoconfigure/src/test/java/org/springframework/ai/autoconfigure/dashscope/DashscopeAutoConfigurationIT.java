package org.springframework.ai.autoconfigure.dashscope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.ai.dashscope.qwen.QWenEmbeddingClient;
import org.springframework.ai.dashscope.qwen.QWenImageClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    void transcribe(){

    }

    @Test
    void speech(){

    }

    @Test
    void generateStreaming(){
        contextRunner.run(context -> {
           QWenChatClient chatClient = context.getBean(QWenChatClient.class);
            Flux<ChatResponse> responseFlux = chatClient.stream(new Prompt(new UserMessage("Hello")));
            String response = responseFlux.collectList().block().stream().map(chatResponse -> chatResponse.getResults().get(0).getOutput().getContent()).collect(Collectors.joining());

            assertThat(response).isNotEmpty();
            logger.info("Resource:"+response);
        });
    }

    @Test
    void embedding(){
        contextRunner.run(context -> {
            QWenEmbeddingClient embeddingClient = context.getBean(QWenEmbeddingClient.class);
            EmbeddingResponse embeddingResponse = embeddingClient.embedForResponse(List.of("Hello Word","World is big and salvation is near"));
            assertThat(embeddingResponse.getResults()).hasSize(2);
            assertThat(embeddingResponse.getResults().get(0).getOutput()).isNotEmpty();
            assertThat(embeddingResponse.getResults().get(0).getIndex()).isEqualTo(0);
            assertThat(embeddingResponse.getResults().get(1).getOutput()).isNotEmpty();
            assertThat(embeddingResponse.getResults().get(1).getIndex()).isEqualTo(1);

            logger.info(embeddingResponse.getResult().getOutput());
        });
    }

    @Test
    void generateImage(){
        contextRunner.withPropertyValues("spring.ai.dashscope.qwen.image.options.size=1024*1024").run(context -> {
            QWenImageClient imageClient = context.getBean(QWenImageClient.class);
            ImageResponse imageResponse = imageClient.call(new ImagePrompt("forest"));
            assertThat(imageResponse.getResults()).hasSize(1);
            assertThat(imageResponse.getResult().getOutput().getUrl()).isNotEmpty();
            logger.info("Generated Image:"+ imageResponse.getResult().getOutput().getUrl());
        });
    }
}
