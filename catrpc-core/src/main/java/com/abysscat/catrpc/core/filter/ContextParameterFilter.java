package com.abysscat.catrpc.core.filter;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 上下文参数过滤器
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/7 14:50
 */
public class ContextParameterFilter implements Filter {
	@Override
	public Object preFilter(RpcRequest request) {
		Map<String, String> params = RpcContext.ContextParameters.get();
		if (!params.isEmpty()) {
			request.getParams().putAll(params);
		}
		return null;
	}

	@Override
	public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
		RpcContext.ContextParameters.get().clear();
		return null;
	}
}
