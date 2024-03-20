package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.annotation.CatConsumer;
import com.abysscat.catrpc.core.api.LoadBalancer;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.Router;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.utils.FieldUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者启动类
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 22:50
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

	ApplicationContext applicationContext;
	Environment environment;

	@Value("${app.id}")
	private String app;

	@Value("${app.namespace}")
	private String namespace;

	@Value("${app.env}")
	private String env;

	@Value("${app.version}")
	private String version;

	/**
	 * Service Consumers Proxy Map
	 * key: service interface class canonical name
	 * value: proxy instance
	 */
	private Map<String, Object> stub = new HashMap<>();

	public void start() {

		Router router = applicationContext.getBean(Router.class);
		LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);

		RpcContext context = new RpcContext();
		context.setRouter(router);
		context.setLoadBalancer(loadBalancer);

		RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

		String[] names = applicationContext.getBeanDefinitionNames();
		for (String name : names) {
			Object bean = applicationContext.getBean(name);

			// 过滤掉spring、jdk、其他框架自身的bean
			String packageName = bean.getClass().getPackageName();
			if (packageName.startsWith("org.springframework.") ||
					packageName.startsWith("java.") ||
					packageName.startsWith("javax.") ||
					packageName.startsWith("jdk.") ||
					packageName.startsWith("com.fasterxml.") ||
					packageName.startsWith("com.sun.") ||
					packageName.startsWith("jakarta.") ||
					packageName.startsWith("org.apache.")) {
				// 降低一半启动耗时
				continue;
			}

			List<Field> fields = FieldUtils.findAnnotatedField(bean.getClass(), CatConsumer.class);
			fields.forEach(f -> {
				try {
					Class<?> service = f.getType();
					String serviceName = service.getCanonicalName();
					Object consumer = stub.get(serviceName);
					if (consumer == null) {
						consumer = createConsumerFromRegistry(service, context, registryCenter);
						stub.put(serviceName, consumer);
					}
					f.setAccessible(true);
					f.set(bean, consumer);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private Object createConsumerFromRegistry(Class<?> service, RpcContext context, RegistryCenter registryCenter) {
		String serviceName = service.getCanonicalName();
		ServiceMeta serviceMeta = ServiceMeta.builder()
				.app(app).namespace(namespace).env(env).name(serviceName).version(version)
				.build();
		List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
		System.out.println("createConsumerFromRegistry providers: ");
		providers.forEach(System.out::println);

		registryCenter.subscribe(serviceMeta, event -> {
			providers.clear();
			providers.addAll(event.getData());
		});

		return createConsumerProxyInstance(service, context, providers);
	}

	private Object createConsumerProxyInstance(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
		return Proxy.newProxyInstance(service.getClassLoader(),
				new Class[]{service},
				new CatInvocationHandler(service, context, providers)
		);
	}

}
