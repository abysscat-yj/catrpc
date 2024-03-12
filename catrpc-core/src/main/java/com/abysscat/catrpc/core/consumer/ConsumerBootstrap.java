package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.annotation.CatConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 22:50
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware {

	ApplicationContext applicationContext;

	/**
	 * Service Consumers Proxy Map
	 * key: service interface class canonical name
	 * value: proxy instance
	 */
	private Map<String, Object> stub = new HashMap<>();

	public void start() {
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

			List<Field> fields = findAnnotatedField(bean.getClass());
			fields.forEach(f -> {
				try {
					Class<?> service = f.getType();
					String serviceName = service.getCanonicalName();
					Object consumer = stub.get(serviceName);
					if (consumer == null) {
						consumer = createConsumerProxyInstance(service);
					}
					f.setAccessible(true);
					f.set(bean, consumer);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private Object createConsumerProxyInstance(Class<?> service) {
		return Proxy.newProxyInstance(service.getClassLoader(),
				new Class[]{service},
				new CatInvocationHandler(service)
		);
	}

	private List<Field> findAnnotatedField(Class<?> aClass) {
		List<Field> result = new ArrayList<>();
		while (aClass != null) {
			Field[] fields = aClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(CatConsumer.class)) {
					result.add(field);
				}
			}
			// 由于容器里的CatrpcDemoConsumerApplication类是被SpringCGlib代理过的子类，所以默认拿不到userService field
			aClass = aClass.getSuperclass();
		}

		return result;
	}
}
