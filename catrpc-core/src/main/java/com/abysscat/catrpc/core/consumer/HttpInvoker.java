package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 1:16
 */
public interface HttpInvoker {

	RpcResponse<?> post(RpcRequest rpcRequest, String url);

}
