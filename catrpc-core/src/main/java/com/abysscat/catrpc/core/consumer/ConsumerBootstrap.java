package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.annotation.CatConsumer;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.utils.FieldUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

	ApplicationContext applicationContext;
	Environment environment;

	/**
	 * Service Consumers Proxy Map
	 * key: service interface class canonical name
	 * value: proxy instance
	 */
	private Map<String, Object> stub = new HashMap<>();

	public void start() {
		RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
		RpcContext context = applicationContext.getBean(RpcContext.class);

		String[] names = applicationContext.getBeanDefinitionNames();
		for (String name : names) {
			Object bean = applicationContext.getBean(name);

			// 过滤掉spring、jdk、其他框架自身的bean
			if (isExternalPackage(bean.getClass().getPackageName())) {
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

	private static boolean isExternalPackage(String packageName) {
		return packageName.startsWith("org.springframework.") ||
				packageName.startsWith("java.") ||
				packageName.startsWith("javax.") ||
				packageName.startsWith("jdk.") ||
				packageName.startsWith("com.fasterxml.") ||
				packageName.startsWith("com.sun.") ||
				packageName.startsWith("jakarta.") ||
				packageName.startsWith("org.apache.");
	}

	private Object createConsumerFromRegistry(Class<?> service, RpcContext context, RegistryCenter registryCenter) {
		ServiceMeta serviceMeta = ServiceMeta.builder()
				.app(context.param("app.id"))
				.namespace(context.param("app.namespace"))
				.env(context.param("app.env"))
				.version(context.param("app.version"))
				.name(service.getCanonicalName())
				.build();
		List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
		log.info("createConsumerFromRegistry providers: ");
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
