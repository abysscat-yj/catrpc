package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 23:18
 */
public class CatInvocationHandler implements InvocationHandler {

	final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

	Class<?> service;

	public CatInvocationHandler(Class<?> clazz) {
		this.service = clazz;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setService(service.getCanonicalName());
		rpcRequest.setMethod(method.getName());
		rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
		rpcRequest.setArgs(args);

		RpcResponse rpcResponse = post(rpcRequest);

		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			if (data instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject) rpcResponse.getData();
				return jsonObject.toJavaObject(method.getReturnType());
			} else {
				return TypeUtils.castToJavaBean(data, method.getReturnType());
			}
		} else {
			Exception ex = rpcResponse.getEx();
			throw new RuntimeException(ex);
		}

	}

	OkHttpClient client = new OkHttpClient.Builder()
			.connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
			.readTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.connectTimeout(60, TimeUnit.SECONDS)
			.build();

	private RpcResponse post(RpcRequest rpcRequest) {
		String reqJson = JSON.toJSONString(rpcRequest);
		Request request = new Request.Builder()
				.url("http://localhost:8080/")
				.post(RequestBody.create(reqJson, JSON_TYPE))
				.build();
		try {
			String respJson = client.newCall(request).execute().body().string();
			RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
			return rpcResponse;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
