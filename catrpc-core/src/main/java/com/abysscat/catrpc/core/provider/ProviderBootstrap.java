package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.meta.ProviderMeta;
import com.abysscat.catrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
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
     * value: providerMeta list -> method level
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    RegistryCenter rc;


    @Value("${server.port}")
    private String port;

    private String instance;

    @PostConstruct
    public void init() {
        rc = applicationContext.getBean(RegistryCenter.class);

        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CatProvider.class);
        providers.forEach((x, y) -> System.out.println(x));

        providers.values().forEach(this::genInterface);

    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = ip + "_" + port;
        // 将服务注册到注册中心
        // 注：得保证服务注册到注册中心时，spring上下文已经初始化完成，才能对外暴露服务
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        rc.register(service, instance);
    }

    private void unregisterService(String service) {
        rc.unregister(service, instance);
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
        skeleton.add(anInterface.getCanonicalName(), meta);
    }

}
