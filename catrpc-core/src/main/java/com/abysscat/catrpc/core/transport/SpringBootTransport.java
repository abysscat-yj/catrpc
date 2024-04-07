package com.abysscat.catrpc.core.transport;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringBoot Framework Endpoint
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/7 15:48
 */
@RestController
public class SpringBootTransport {

	@Autowired
	ProviderInvoker providerInvoker;

	@RequestMapping("/catrpc")
	public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
		return providerInvoker.invoke(request);
	}

}
