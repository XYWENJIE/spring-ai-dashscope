package org.springframework.ai.dashscope.llama;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DashScopeLlamaChatModelIT {

    @Autowired
    private DashScopeLlamaChatModel chatModel;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    @Test
    void multipleStreamAttempts(){

    }

    @Test
    void roleTest(){
        UserMessage userMessage = new UserMessage("Tell");
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name","Bob","voice","pirate"));

        Prompt prompt = new Prompt(List.of(userMessage,systemMessage));

        ChatResponse chatResponse = chatModel.call(prompt);

        assertThat(chatResponse.getResult().getOutput().getContent()).contains("Blackbeard");
    }

    @SpringBootConfiguration
    public static class TestConfiguration{


    }
}
