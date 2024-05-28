//package org.springframework.ai.dashscope.chat.service;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
//import org.springframework.ai.chat.memory.*;
//import org.springframework.ai.chat.service.ChatService;
//import org.springframework.ai.chat.service.PromptTransformingChatService;
//import org.springframework.ai.chat.service.StreamingChatService;
//import org.springframework.ai.chat.service.StreamingPromptTransformingChatService;
//import org.springframework.ai.dashscope.DashsCopeService;
//import org.springframework.ai.dashscope.qwen.QWenChatModel;
//import org.springframework.ai.evaluation.BaseMemoryTest;
//import org.springframework.ai.evaluation.RelevancyEvaluator;
//import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
//import org.springframework.ai.tokenizer.TokenCountEstimator;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Bean;
//
//import java.util.List;
//
//@SpringBootTest(classes = ChatMemoryShortTermMessageListIT.Config.class)
//@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY",matches = ".+")
//public class ChatMemoryShortTermMessageListIT extends BaseMemoryTest{
//
//    private Logger logger = LogManager.getLogger();
//
//    @Autowired
//    public ChatMemoryShortTermMessageListIT(RelevancyEvaluator relevancyEvaluator, ChatService memoryChatService, StreamingChatService memoryStreamingChatService) {
//        super(relevancyEvaluator, memoryChatService, memoryStreamingChatService);
//    }
//
//    @Test
//    public void abc(){
//    }
//
//    @SpringBootConfiguration
//    public static class Config{
//
//        @Bean
//        public DashsCopeService dashsCopeService(){
//            return new DashsCopeService(System.getenv("DASHSCOPE_API_KEY"));
//        }
//
//        @Bean
//        public QWenChatModel qWenChatClient(DashsCopeService dashsCopeService){
//            return new QWenChatModel(dashsCopeService);
//        }
//
//        @Bean
//        public ChatMemory chatHistory(){
//            return new InMemoryChatMemory();
//        }
//
//        @Bean
//        public TokenCountEstimator tokenCountEstimator(){
//            return new JTokkitTokenCountEstimator();
//        }
//
//        @Bean
//        public ChatService memoryChatService(QWenChatModel qWenChatClient, ChatMemory chatHistory,
//                                             TokenCountEstimator tokenCountEstimator){
//            return PromptTransformingChatService.builder(qWenChatClient)
//                    .withRetrievers(List.of(new ChatMemoryRetriever(chatHistory)))
//                    .withContentPostProcessors(List.of(new LastMaxTokenSizeContentTransformer(tokenCountEstimator,1000)))
//                    .withAugmentors(List.of(new MessageChatMemoryAugmentor()))
//                    .withChatServiceListeners(List.of(new ChatMemoryChatServiceListener(chatHistory)))
//                    .build();
//        }
//
//        @Bean
//        public StreamingChatService memoryStreamingChatService(QWenChatModel streamingChatClient,
//                                                               ChatMemory chatHistory, TokenCountEstimator tokenCountEstimator) {
//
//            return StreamingPromptTransformingChatService.builder(streamingChatClient)
//                    .withRetrievers(List.of(new ChatMemoryRetriever(chatHistory)))
//                    .withDocumentPostProcessors(List.of(new LastMaxTokenSizeContentTransformer(tokenCountEstimator, 1000)))
//                    .withAugmentors(List.of(new MessageChatMemoryAugmentor()))
//                    .withChatServiceListeners(List.of(new ChatMemoryChatServiceListener(chatHistory)))
//                    .build();
//        }
//
//        @Bean
//        public RelevancyEvaluator relevancyEvaluator(QWenChatModel chatClient) {
//            return new RelevancyEvaluator(chatClient);
//        }
//
//    }
//}
