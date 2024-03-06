package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 2:31
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CatProvider.class);
        providers.forEach((x, y) -> System.out.println(x));

        providers.values().forEach(this::genInterface);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void genInterface(Object x) {
        Class<?>[] interfaces = x.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            skeleton.put(anInterface.getCanonicalName(), x);
        }
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
