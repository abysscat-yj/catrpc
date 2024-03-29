package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.api.exception.ErrorEnum;
import com.abysscat.catrpc.core.api.exception.RpcException;
import com.abysscat.catrpc.core.consumer.http.OkHttpInvoker;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
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
	int retries;

	HttpInvoker invoker;

	public CatInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
		this.service = clazz;
		this.context = context;
		this.providers = providers;
		this.retries = Integer.parseInt(
				context.getParameters().getOrDefault("app.retries", "1"));
		int timeout = Integer.parseInt(
				context.getParameters().getOrDefault("app.timeout", "1000"));
		this.invoker =  new OkHttpInvoker(timeout);
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
		Object result = null;

		int currRetries = retries;

		while (currRetries-- > 0) {

			if (currRetries < retries - 1) {
				log.debug("invoke retry ing... retries = " + currRetries);
			}

			try {
				// 前置过滤处理
				for (Filter filter : context.getFilters()) {
					Object preResult = filter.preFilter(rpcRequest);
					if (preResult != null) {
						log.debug(filter.getClass().getName() + " =====> preFilter preResult: " + preResult);
						return preResult;
					}
				}

				List<InstanceMeta> instances = context.getRouter().route(providers);
				InstanceMeta instance = context.getLoadBalancer().choose(instances);

				log.debug("CatInvocationHandler loadBalancer.choose instance: " + instance);

				RpcResponse<?> rpcResponse = invoker.post(rpcRequest, instance.toUrl());

				result = castReturnResult(method, rpcResponse);

				// 后置过滤处理
				for (Filter filter : context.getFilters()) {
					Object postResult = filter.postFilter(rpcRequest, rpcResponse, result);
					if (postResult != null) {
						return postResult;
					}
				}

				return result;

			} catch (Exception e) {
				if (!(e.getCause() instanceof SocketTimeoutException)) {
					throw e;
				}
			}
		}

		return result;
	}

	private Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			return TypeUtils.castMethodResult(method, data);
		} else {
			Exception exception = rpcResponse.getEx();
			if(exception instanceof RpcException ex) {
				throw ex;
			} else {
				throw new RpcException(exception, ErrorEnum.UNKNOWN_ERROR);
			}
		}
	}

}
