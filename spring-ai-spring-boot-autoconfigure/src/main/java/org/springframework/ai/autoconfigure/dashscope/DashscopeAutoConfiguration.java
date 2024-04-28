package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.qwen.QWenChatClient;
import org.springframework.ai.dashscope.qwen.QWenEmbeddingClient;
import org.springframework.ai.dashscope.qwen.QWenImageClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

@AutoConfiguration(after = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class})
@ConditionalOnClass(DashsCopeService.class)
@EnableConfigurationProperties({DashscopeProperties.class, QWenImageProperties.class,DashscopeConnectionProperties.class})
public class DashscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DashscopeProperties.CONFIG_PREFIX,name="enabled",havingValue = "true",matchIfMissing = true)
    public QWenChatClient qWenChatClient(DashscopeProperties dashscopeProperties, List<FunctionCallback> toolFunctionCallback,
                                         FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate){
        Assert.hasText(dashscopeProperties.getApikey(),"Dashscope阿里云的AccessToken不存在");
        var dashsCopeService = dashsCopeService(dashscopeProperties.getApikey());
        if(!CollectionUtils.isEmpty(toolFunctionCallback)){
            dashscopeProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallback);
        }
        return new QWenChatClient(dashsCopeService,dashscopeProperties.getOptions(),functionCallbackContext,retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    public QWenEmbeddingClient qWenEmbeddingClient(DashscopeProperties dashscopeProperties){
        Assert.hasText(dashscopeProperties.getApikey(),"Dashscope阿里云的AccessToken不存在");
        var dashsCopeService = dashsCopeService(dashscopeProperties.getApikey());
        return new QWenEmbeddingClient(dashsCopeService);
    }


    private DashsCopeService dashsCopeService(String accessToken){
        return new DashsCopeService(accessToken);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = QWenImageProperties.CONFIG_PREFIX,name = "enabled",havingValue = "true",
                        matchIfMissing = true)
    public QWenImageClient qWenImageClient(QWenImageProperties qWenImageProperties){
        Assert.hasText(qWenImageProperties.getApikey(),"Dashscope阿里云的AccessToken不存在");
        var dashsCopeService = dashsCopeService(qWenImageProperties.getApikey());
        return new QWenImageClient(dashsCopeService);
    }

    //TODO 未来1.0添加声音模型

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context){
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }
}
