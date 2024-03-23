package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.consumer.http.OkHttpInvoker;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理类
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 23:18
 */
@Slf4j
public class CatInvocationHandler implements InvocationHandler {

	Class<?> service;
	RpcContext context;
	List<InstanceMeta> providers;

	HttpInvoker invoker = new OkHttpInvoker();

	public CatInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
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

		List<InstanceMeta> instances = context.getRouter().route(providers);
		InstanceMeta instance = context.getLoadBalancer().choose(instances);

		log.debug("CatInvocationHandler loadBalancer.choose instance: " + instance);

		RpcResponse<?> rpcResponse = invoker.post(rpcRequest, instance.toUrl());

		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			return TypeUtils.castMethodResult(method, data);
		} else {
			Exception ex = rpcResponse.getEx();
			throw new RuntimeException(ex);
		}

	}

}
