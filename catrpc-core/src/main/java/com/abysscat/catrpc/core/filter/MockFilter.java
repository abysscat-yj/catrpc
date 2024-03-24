package com.abysscat.catrpc.core.filter;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Mock过滤器，实现挡板效果
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/25 0:23
 */
public class MockFilter implements Filter {
	@SneakyThrows
	@Override
	public Object preFilter(RpcRequest request) {
		Class<?> service = Class.forName(request.getService());
		Method method = findMethod(service, request.getMethodSign());
		if (method == null) {
			return null;
		}
		Class clazz = method.getReturnType();
		return MockUtils.mock(clazz);
	}

	private Method findMethod(Class<?> service, String methodSign) {
		return Arrays.stream(service.getMethods())
				.filter(method -> !MethodUtils.checkLocalMethod(method))
				.filter(method -> methodSign.equals(MethodUtils.getMethodSign(method)))
				.findFirst().orElse(null);
	}

	@Override
	public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
		return null;
	}
}
