package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.Filter;
import com.abysscat.catrpc.core.api.RpcContext;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.api.exception.RpcException;
import com.abysscat.catrpc.core.consumer.http.OkHttpInvoker;
import com.abysscat.catrpc.core.governance.SlidingTimeWindow;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	final List<InstanceMeta> providers;
	final List<InstanceMeta> isolateProviders = new ArrayList<>();
	final List<InstanceMeta> halfOpenProviders = new ArrayList<>();

	int retries;
	int faultLimit;
	final Map<String, SlidingTimeWindow> windowMap = new HashMap<>();

	HttpInvoker invoker;

	ScheduledExecutorService executor;

	public CatInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
		this.service = clazz;
		this.context = context;
		this.providers = providers;
		this.retries = Integer.parseInt(
				context.getParameters().getOrDefault("consumer.retries", "1"));
		this.faultLimit = Integer.parseInt(
				context.getParameters().getOrDefault("consumer.faultLimit", "10"));
		int timeout = Integer.parseInt(
				context.getParameters().getOrDefault("consumer.timeout", "1000"));
		this.invoker =  new OkHttpInvoker(timeout);
		this.executor = Executors.newScheduledThreadPool(1);
		int halfOpenInitialDelay = Integer.parseInt(context.getParameters()
				.getOrDefault("consumer.halfOpenInitialDelay", "10000"));
		int halfOpenDelay = Integer.parseInt(context.getParameters()
				.getOrDefault("consumer.halfOpenDelay", "60000"));
		this.executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay,
				halfOpenDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 周期性把隔离节点放到半开节点集合
	 * <p>
	 * 为什么需要单独定义一个 halfOpenProviders，而不是直接用 isolateProviders ?
	 * 因为在探活这个操作是在每次调用路由时做的，非常高频，
	 * 为了性能不需要在 isolateProviders 有值后立马就尝试探活，而是周期性取到隔离节点探活，
	 * 这样即使某个节点持续故障，频繁被加入到 isolateProviders，也只是周期性探活时才会调到这个故障节点。
	 */
	private void halfOpen() {
		log.debug("half open service：{}, isolateProviders: {}", service.getCanonicalName(), isolateProviders);
		halfOpenProviders.clear();
		halfOpenProviders.addAll(isolateProviders);
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

				InstanceMeta instance;
				boolean isHalfOpenInstance = false;
				synchronized (halfOpenProviders) {
					// 是否探活
					if (halfOpenProviders.isEmpty()) {
						List<InstanceMeta> instances = context.getRouter().route(providers);
						instance = context.getLoadBalancer().choose(instances);
						log.debug("CatInvocationHandler loadBalancer.choose instance: " + instance);
					} else {
						instance = halfOpenProviders.remove(0);
						isHalfOpenInstance = true;
						log.debug(" check alive instance ==> {}", instance);
					}
				}

				if (instance == null) {
					log.warn("no instance! rpcRequest: " + rpcRequest);
					return result;
				}

				String url = instance.toUrl();

				RpcResponse<?> rpcResponse;
				try {
					rpcResponse = invoker.post(rpcRequest, url);
					result = castReturnResult(method, rpcResponse);
				} catch (Exception e) {

					// 故障统计隔离
					synchronized (windowMap) {
						SlidingTimeWindow window = windowMap.computeIfAbsent(url, k -> new SlidingTimeWindow());
						window.record(System.currentTimeMillis());

						log.debug("instance {} in window with {}", url, window.getSum());

						if (window.getSum() >= this.faultLimit) {
							isolate(instance);
						}
					}

					throw e;
				}

				// 当前是探活节点，并且执行成功了，则恢复该节点
				if (isHalfOpenInstance) {
					isolateProviders.remove(instance);
					providers.add(instance);
					log.debug("instance {} is recovered, isolateProviders: {}, providers: {}",
							instance, isolateProviders, providers);
				}

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

	private void isolate(InstanceMeta instance) {
		log.debug("===> isolate instance:{}", instance);
		providers.remove(instance);
		log.debug("===> after isolate instance, providers:{}", providers);
		isolateProviders.add(instance);
		log.debug("===> after isolate instance, isolateProviders:{}", isolateProviders);
	}

	private Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			return TypeUtils.castMethodResult(method, data);
		} else {
			RpcException exception = rpcResponse.getEx();
			if(exception != null) {
				log.error("rpc response error.", exception);
				throw exception;
			}
			return null;
		}
	}

}
