package com.abysscat.catrpc.core.utils;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/11 1:25
 */
public class MethodUtils {

	public static boolean checkLocalMethod(final String method) {
		//本地方法不代理
		return "toString".equals(method) ||
				"hashCode".equals(method) ||
				"notifyAll".equals(method) ||
				"equals".equals(method) ||
				"wait".equals(method) ||
				"getClass".equals(method) ||
				"notify".equals(method);
	}

}
