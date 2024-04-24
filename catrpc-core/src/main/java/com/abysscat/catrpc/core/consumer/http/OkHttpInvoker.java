package com.abysscat.catrpc.core.consumer.http;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 1:17
 */
public class OkHttpInvoker implements HttpInvoker {

	final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

	OkHttpClient client;

	public OkHttpInvoker(int timeout) {
		client = new OkHttpClient.Builder()
				.connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
				.readTimeout(timeout, TimeUnit.MILLISECONDS)
				.writeTimeout(timeout, TimeUnit.MILLISECONDS)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.build();
	}

	@Override
	public RpcResponse<?> post(RpcRequest rpcRequest, String url) {

		String reqJson = JSON.toJSONString(rpcRequest);
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(reqJson, JSON_TYPE))
				.build();
		try {
			String respJson = client.newCall(request).execute().body().string();
			RpcResponse<Object> rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
			return rpcResponse;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String post(String requestString, String url) {
		log.debug(" ===> post  url = {}, requestString = {}", requestString, url);
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(requestString, JSON_TYPE))
				.build();
		try {
			String respJson = client.newCall(request).execute().body().string();
			log.debug(" ===> respJson = " + respJson);
			return respJson;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String get(String url) {
		log.debug(" ===> get url = " + url);
		Request request = new Request.Builder()
				.url(url)
				.get()
				.build();
		try {
			String respJson = client.newCall(request).execute().body().string();
			log.debug(" ===> respJson = " + respJson);
			return respJson;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
