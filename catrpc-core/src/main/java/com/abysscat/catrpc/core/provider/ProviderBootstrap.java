package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
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
        // 过滤本地默认方法， 不对外提供反射调用
        if (MethodUtils.checkLocalMethod(request.getMethod())) {
            return null;
        }

        RpcResponse rpcResponse = new RpcResponse();
        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
		return rpcResponse;
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
