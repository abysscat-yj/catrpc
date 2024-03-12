package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.alibaba.fastjson.util.TypeUtils;
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

    /**
     * Service Providers Map
     * key: service interface class canonical name
     * value: bean instance
     */
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
            Method method = findMethod(bean.getClass(), request.getMethod(), request.getMethodSign());
            if (method == null) {
                rpcResponse.setStatus(false);
                rpcResponse.setEx(new RuntimeException("no such method:" + request.getMethod()));
                return rpcResponse;
            }
            // 由于 request 中的 Object[] args 可能丢失掉基本类型、包装类，所以需要转换
            Object[] args = castArgsType(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(bean, args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
		return rpcResponse;
    }

    private Object[] castArgsType(Object[] args, Class<?>[] parameterTypes) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = TypeUtils.castToJavaBean(args[i], parameterTypes[i]);
        }
        return result;
    }

    private void genInterface(Object x) {
        Class<?>[] interfaces = x.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            skeleton.put(anInterface.getCanonicalName(), x);
        }
    }

    private Method findMethod(Class<?> aClass, String methodName, String methodSign) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName) && MethodUtils.getMethodSign(method).equals(methodSign)) {
                return method;
            }
        }
        return null;
    }

}
