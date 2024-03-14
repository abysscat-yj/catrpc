package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.meta.ProviderMeta;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * value: providerMeta list -> method level
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CatProvider.class);
        providers.forEach((x, y) -> System.out.println(x));

        providers.values().forEach(this::genInterface);
    }

    public RpcResponse invoke(RpcRequest request) {
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        RpcResponse rpcResponse = new RpcResponse();
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            if (meta == null) {
                rpcResponse.setStatus(false);
                rpcResponse.setEx(new RuntimeException("no such method, request:" + request));
                return rpcResponse;
            }
            Method method = meta.getMethod();
            Object bean = meta.getServiceImpl();
            // 由于 request 中的 Object[] args 可能丢失掉基本类型、包装类、对象类型，所以需要转换
            Object[] args = castArgsType(request.getArgs(), method);
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

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        if (CollectionUtils.isEmpty(providerMetas)) {
            return null;
        }
        Optional<ProviderMeta> providerMeta = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign))
                .findFirst();
        return providerMeta.orElse(null);
    }

    private Object[] castArgsType(Object[] args, Method method) {
        if(args == null || args.length == 0) return args;

        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof List list) {
                List<Object> resultList = new ArrayList<>(list.size());
                Type genericParameterType = method.getGenericParameterTypes()[i];
                if (genericParameterType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    for (Object o : list) {
                        resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
                    }
                    result[i] = resultList;
                } else {
                    result[i] = TypeUtils.cast(args[i], method.getParameterTypes()[i]);
                }
            } else {
                result[i] = TypeUtils.cast(args[i], method.getParameterTypes()[i]);
            }
        }
        return result;
    }

    private void genInterface(Object x) {
        Class<?>[] interfaces = x.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            Method[] methods = anInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(anInterface, x, method);
            }
        }
    }

    private void createProvider(Class<?> anInterface, Object x, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setMethodSign(MethodUtils.getMethodSign(method));
        meta.setServiceImpl(x);
        System.out.println("createProvider meta:" + meta);
        skeleton.add(anInterface.getCanonicalName(), meta);
    }

}
