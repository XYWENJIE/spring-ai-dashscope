package org.springframework.ai.autoconfigure.dashscope;

import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties({DashsCopeAliyunConnectionProperties.class})
public class DashsCopeAliyunConnectionConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DashsCopeService dashsCopeService(DashsCopeAliyunConnectionProperties connectionProperties){
        if(StringUtils.hasText(connectionProperties.getAccessToken())){
            return new DashsCopeService(connectionProperties.getAccessToken());
        }
        return new DashsCopeService(System.getenv("DASHSCOPE_API_KEY"));
    }
}
