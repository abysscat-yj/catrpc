package com.abysscat.catrpc.core.config;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.LoadBalancer;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.Router;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.cluster.GrayRouter;
import com.abysscat.catrpc.core.cluster.RoundRobinBalancer;
import com.abysscat.catrpc.core.consumer.ConsumerBootstrap;
import com.abysscat.catrpc.core.filter.ContextParameterFilter;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 22:53
 */
@Configuration
@Import({AppConfigProperties.class,ConsumerConfigProperties.class})
public class ConsumerConfig {

	@Autowired
	AppConfigProperties appConfigProperties;

	@Autowired
	ConsumerConfigProperties consumerConfigProperties;

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
		return new GrayRouter(consumerConfigProperties.getGrayRatio());
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

	@Bean
	public Filter contextParameterFilter() {
		return new ContextParameterFilter();
	}

	@Bean
	public RpcContext createContext(@Autowired Router router,
									@Autowired LoadBalancer loadBalancer,
									@Autowired List<Filter> filters) {
		RpcContext context = new RpcContext();
		context.setRouter(router);
		context.setLoadBalancer(loadBalancer);
		context.setFilters(filters);
		context.getParameters().put("app.id", appConfigProperties.getId());
		context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
		context.getParameters().put("app.env", appConfigProperties.getEnv());
		context.getParameters().put("app.version", appConfigProperties.getVersion());
		context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
		context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
		context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
		context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
		context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
		return context;
	}

}
