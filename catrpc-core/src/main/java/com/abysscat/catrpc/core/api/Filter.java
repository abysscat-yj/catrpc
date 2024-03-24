package com.abysscat.catrpc.core.api;

/**
 * 过滤器
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 17:38
 */
public interface Filter {

	Object preFilter(RpcRequest request);

	Object postFilter(RpcRequest request, RpcResponse<?> response, Object result);

	Filter Default = new Filter() {
		@Override
		public Object preFilter(RpcRequest request) {
			return null;
		}

		@Override
		public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
			return null;
		}
	};

}
