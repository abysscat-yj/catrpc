package com.abysscat.catrpc.core.api;

import java.util.List;

/**
 * 负载均衡器
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 17:39
 */
public interface LoadBalancer<T> {

	T choose(List<T> providers);

	LoadBalancer Default = providers -> {
		if (providers == null || providers.isEmpty()) {
			return null;
		}
		return providers.get(0);
	};

}
