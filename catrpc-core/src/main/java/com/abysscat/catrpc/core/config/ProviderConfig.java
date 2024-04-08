package com.abysscat.catrpc.core.config;

import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.provider.ProviderBootstrap;
import com.abysscat.catrpc.core.provider.ProviderInvoker;
import com.abysscat.catrpc.core.registry.zk.ZkRegistryCenter;
import com.abysscat.catrpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 2:34
 */
@Configuration
@Slf4j
@Import({AppProperties.class, ProviderProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private String port;

    @Autowired
	AppProperties appProperties;

    @Autowired
    ProviderProperties providerProperties;

    @Bean
    ProviderBootstrap createProviderBootstrap() {
        return new ProviderBootstrap(port, appProperties, providerProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("providerBootstrap starting ...");
            providerBootstrap.start();
            log.info("providerBootstrap started ...");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter providerRC() {
        return new ZkRegistryCenter();
    }

}
