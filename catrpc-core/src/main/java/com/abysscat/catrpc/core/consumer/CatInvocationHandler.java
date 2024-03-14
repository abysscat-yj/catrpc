package com.abysscat.catrpc.core.consumer;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.utils.MethodUtils;
import com.abysscat.catrpc.core.utils.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		// 过滤本地默认方法， 不对外提供反射调用
		if (MethodUtils.checkLocalMethod(method.getName())) {
			return null;
		}

		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setService(service.getCanonicalName());
		rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
		rpcRequest.setArgs(args);

		RpcResponse rpcResponse = post(rpcRequest);

		if (rpcResponse.isStatus()) {
			Object data = rpcResponse.getData();
			Class<?> type = method.getReturnType();
			if (data instanceof JSONObject jsonResult) {
				if (Map.class.isAssignableFrom(type)) {
					Map resultMap = new HashMap();
					Type genericReturnType = method.getGenericReturnType();
					if (genericReturnType instanceof ParameterizedType parameterizedType) {
						Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
						Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
						jsonResult.entrySet().stream().forEach(
								e -> {
									Object key = TypeUtils.cast(e.getKey(), keyType);
									Object value = TypeUtils.cast(e.getValue(), valueType);
									resultMap.put(key, value);
								}
						);
					}
					return resultMap;
				}
				return jsonResult.toJavaObject(type);
			} else if (data instanceof JSONArray jsonArray) {
				Object[] array = jsonArray.toArray();
				if (type.isArray()) {
					Class<?> componentType = type.getComponentType();
					Object resultArray = Array.newInstance(componentType, array.length);
					for (int i = 0; i < array.length; i++) {
						Array.set(resultArray, i, array[i]);
					}
					return resultArray;
				} else if (List.class.isAssignableFrom(type)) {
					List<Object> resultList = new ArrayList<>(array.length);
					Type genericReturnType = method.getGenericReturnType();
					if (genericReturnType instanceof ParameterizedType parameterizedType) {
						Type actualType = parameterizedType.getActualTypeArguments()[0];
						for (Object o : array) {
							resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
						}
					} else {
						resultList.addAll(Arrays.asList(array));
					}
					return resultList;
				} else {
					return null;
				}
			} else {
				return TypeUtils.cast(data, type);
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
