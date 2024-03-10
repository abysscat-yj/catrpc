package com.abysscat.catrpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 22:53
 */
@Configuration
public class ConsumerConfig {

    @Bean
	ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

	@Bean
	@Order(Integer.MIN_VALUE) // 此处需要调高优先级，否则外层runner会先执行
	public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
		return x -> {
			consumerBootstrap.start();
		};
	}

}
