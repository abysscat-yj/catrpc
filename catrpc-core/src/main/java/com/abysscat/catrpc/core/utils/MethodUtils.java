package com.abysscat.catrpc.core.utils;

import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/11 1:25
 */
public class MethodUtils {

	public static boolean checkLocalMethod(final String method) {
		// 本地方法不代理
		return "toString".equals(method) ||
				"hashCode".equals(method) ||
				"notifyAll".equals(method) ||
				"equals".equals(method) ||
				"wait".equals(method) ||
				"getClass".equals(method) ||
				"notify".equals(method);
	}

	public static boolean checkLocalMethod(final Method method) {
		return method.getDeclaringClass().equals(Object.class);
	}

	/**
	 * 生成方法签名字符串
	 * @param method 反射 method 对象
	 * @return method sign string
	 */
	public static String getMethodSign(final Method method) {
		if (method == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder(method.getName());
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (Class<?> type : parameterTypes) {
			builder.append(":");
			builder.append(type.getName());
		}
		return DigestUtils.md5DigestAsHex(builder.toString().getBytes());
	}

}
