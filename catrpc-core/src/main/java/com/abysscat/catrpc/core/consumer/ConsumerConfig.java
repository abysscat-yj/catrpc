package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.LoadBalancer;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.Router;
import com.abysscat.catrpc.core.cluster.GrayRouter;
import com.abysscat.catrpc.core.cluster.RoundRobinBalancer;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

	@Value("${app.grayRatio}")
	int grayRatio;


    @Bean
	ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

	@Bean
	@Order(Integer.MIN_VALUE + 1) // 此处需要调高优先级，否则外层runner会先执行
	public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
		return x -> {
			consumerBootstrap.start();
		};
	}

	@Bean
	public LoadBalancer<InstanceMeta> loadBalancer() {
//		return LoadBalancer.Default;
//		return new RandomLoadBalancer();
		return new RoundRobinBalancer<>();
	}

	@Bean
	public Router<InstanceMeta> router() {
		return new GrayRouter(grayRatio);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean
	public RegistryCenter consumerRC() {
		return new ZkRegistryCenter();
	}

//	@Bean
//	public Filter cacheFilter() {
//		return new CacheFilter();
//	}

//	@Bean
//	public Filter mockFilter() {
//		return new MockFilter();
//	}

}
