package com.abysscat.catrpc.core.provider;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.meta.ProviderMeta;
import com.abysscat.catrpc.core.utils.TypeUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 0:46
 */
public class ProviderInvoker {

	private MultiValueMap<String, ProviderMeta> skeleton;

	public ProviderInvoker(ProviderBootstrap providerBootstrap) {
		this.skeleton = providerBootstrap.getSkeleton();
	}

	public RpcResponse<Object> invoke(RpcRequest request) {
		List<ProviderMeta> providerMetas = skeleton.get(request.getService());
		RpcResponse<Object> rpcResponse = new RpcResponse<>();
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

}
