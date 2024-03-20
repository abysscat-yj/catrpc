package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 2:34
 */
@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootstrap createProviderBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            System.out.println("providerBootstrap starting ...");
            providerBootstrap.start();
            System.out.println("providerBootstrap started ...");
        };
    }

    @Bean
    public RegistryCenter providerRC() {
        return new ZkRegistryCenter();
    }

}
