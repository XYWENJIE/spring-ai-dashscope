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
import org.springframework.util.StringUtils;

import java.util.List;

@AutoConfiguration(after = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class})
@ConditionalOnClass(DashsCopeService.class)
@EnableConfigurationProperties({DashscopeProperties.class, QWenImageProperties.class,DashscopeConnectionProperties.class,DashscopeEmbeddingProperties.class})
public class DashscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DashscopeProperties.CONFIG_PREFIX,name="enabled",havingValue = "true",matchIfMissing = true)
    public QWenChatClient qWenChatClient(DashscopeConnectionProperties commonProperties, DashscopeProperties dashscopeProperties, List<FunctionCallback> toolFunctionCallback,
                                         FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate){
        var dashsCopeService = dashsCopeService(dashscopeProperties.getApikey(),commonProperties.getApikey());
        if(!CollectionUtils.isEmpty(toolFunctionCallback)){
            dashscopeProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallback);
        }
        return new QWenChatClient(dashsCopeService,dashscopeProperties.getOptions(),functionCallbackContext,retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DashscopeEmbeddingProperties.CONFIG_PREFIX,name = "enabled",havingValue = "true",
            matchIfMissing = true)
    public QWenEmbeddingClient qWenEmbeddingClient(DashscopeConnectionProperties commonProperties,DashscopeProperties dashscopeProperties){
        var dashsCopeService = dashsCopeService(dashscopeProperties.getApikey(),commonProperties.getApikey());
        return new QWenEmbeddingClient(dashsCopeService);
    }


    private DashsCopeService dashsCopeService(String apikey,String commonApiKey){
        String resolvedApiKey = StringUtils.hasText(apikey) ? apikey : commonApiKey;
        Assert.hasText(resolvedApiKey,"Dashscope阿里云的AccessToken不存在");
        return new DashsCopeService(resolvedApiKey);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = QWenImageProperties.CONFIG_PREFIX,name = "enabled",havingValue = "true",
                        matchIfMissing = true)
    public QWenImageClient qWenImageClient(DashscopeConnectionProperties commonProperties,QWenImageProperties qWenImageProperties){
        var dashsCopeService = dashsCopeService(qWenImageProperties.getApikey(),commonProperties.getApikey());
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
