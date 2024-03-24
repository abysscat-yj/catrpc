package com.abysscat.catrpc.core.filter;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

/**
 * 缓存过滤器
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/24 16:25
 */
@Order(value = Integer.MAX_VALUE)
public class CacheFilter implements Filter {

	Cache<String, Object> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	@Override
	public Object preFilter(RpcRequest request) {
		return cache.getIfPresent(request.toString());
	}

	@Override
	public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
		cache.put(request.toString(), result);
		return result;
	}

}
