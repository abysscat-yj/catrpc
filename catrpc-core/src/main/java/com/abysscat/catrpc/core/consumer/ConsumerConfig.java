package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.LoadBalancer;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.Router;
import com.abysscat.catrpc.core.cluster.RoundRobinBalancer;
import com.abysscat.catrpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${catrpc.providers}")
	String providers;


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

	@Bean
	public LoadBalancer<?> loadBalancer() {
//		return LoadBalancer.Default;
//		return new RandomLoadBalancer();
		return new RoundRobinBalancer<>();
	}

	@Bean
	public Router<?> router() {
		return Router.Default;
	}

	@Bean
	public RegistryCenter consumerRC() {
		return new ZkRegistryCenter();
	}

}
