package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.consumer.http.OkHttpInvoker;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理类
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 23:18
 */
public class CatInvocationHandler implements InvocationHandler {

	Class<?> service;
	RpcContext context;
	List<String> providers;

	HttpInvoker invoker = new OkHttpInvoker();

	public CatInvocationHandler(Class<?> clazz, RpcContext context, List<String> providers) {
		this.service = clazz;
		this.context = context;
		this.providers = providers;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// 过滤本地默认方法， 不对外提供反射调用
		if (MethodUtils.checkLocalMethod(method.getName())) {
			return null;
		}

		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setService(service.getCanonicalName());
		rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
		rpcRequest.setArgs(args);

		List<String> urls = context.getRouter().route(providers);
		String url = (String) context.getLoadBalancer().choose(urls);

		System.out.println("CatInvocationHandler loadBalancer.choose url: " + url);

		RpcResponse rpcResponse = invoker.post(rpcRequest, url);

		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			return TypeUtils.castMethodResult(method, data);
		} else {
			Exception ex = rpcResponse.getEx();
			throw new RuntimeException(ex);
		}

	}

}
