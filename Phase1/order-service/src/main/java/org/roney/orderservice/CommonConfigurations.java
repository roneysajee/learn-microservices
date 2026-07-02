package org.roney.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CommonConfigurations {

    @Value("${inventory.service.url}")
    private String inventoryUrl;
    
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
        .baseUrl(inventoryUrl)
        .build();
    }
}
