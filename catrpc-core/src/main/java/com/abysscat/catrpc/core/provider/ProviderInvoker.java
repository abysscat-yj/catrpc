package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.exception.ErrorEnum;
import com.abysscat.catrpc.core.exception.RpcException;
import com.abysscat.catrpc.core.config.ProviderProperties;
import com.abysscat.catrpc.core.governance.SlidingTimeWindow;
import com.abysscat.catrpc.core.meta.ProviderMeta;
import com.abysscat.catrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 0:46
 */
@Slf4j
public class ProviderInvoker {

	private MultiValueMap<String, ProviderMeta> skeleton;

	final Map<String, SlidingTimeWindow> windows = new HashMap<>();

	final ProviderProperties providerProperties;

	public ProviderInvoker(ProviderBootstrap providerBootstrap) {
		this.skeleton = providerBootstrap.getSkeleton();
		this.providerProperties = providerBootstrap.getProviderProperties();
	}

	public RpcResponse<Object> invoke(RpcRequest request) {
		log.debug(" ===> ProviderInvoker.invoke(request:{})", request);

		// request 上下文参数传递
		Map<String, String> params = request.getParams();
		if(!params.isEmpty()) {
			params.forEach(RpcContext::setContextParameter);
		}

		// 限流
		String service = request.getService();
		// todo 按服务粒度限流
		// todo 分布式节点共享限流, redis
		// todo 令牌桶和漏桶
		int trafficControl = Integer.parseInt(providerProperties.getMetas().getOrDefault("tc", "20"));
		log.debug(" ===>> trafficControl:{} for {}", trafficControl, service);
		synchronized (windows) {
			SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
			if (window.calcSum() >= trafficControl) {
				throw new RpcException("service " + service + " invoked in 30s/[" +
						window.getSum() + "] larger than tpsLimit = " + trafficControl, ErrorEnum.EXCEED_LIMIT_ERROR);
			}

			window.record(System.currentTimeMillis());
			log.debug("service {} in window with {}", service, window.getSum());
		}

		List<ProviderMeta> providerMetas = skeleton.get(request.getService());
		RpcResponse<Object> rpcResponse = new RpcResponse<>();
		try {
			ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
			if (meta == null) {
				rpcResponse.setStatus(false);
				rpcResponse.setEx(new RpcException(ErrorEnum.NO_SUCH_METHOD_ERROR));
				return rpcResponse;
			}
			Method method = meta.getMethod();
			Object bean = meta.getServiceImpl();
			// 由于 request 中的 Object[] args 可能丢失掉基本类型、包装类、对象类型，所以需要转换
			Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
			Object result = method.invoke(bean, args);
			rpcResponse.setStatus(true);
			rpcResponse.setData(result);
		} catch (InvocationTargetException e) {
			rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
		} catch (IllegalAccessException e) {
			rpcResponse.setEx(new RpcException(e.getMessage()));
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

	private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
		if(args == null || args.length == 0) return args;
		Object[] actual = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			actual[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
		}
		return actual;
	}

}
