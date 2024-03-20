package com.abysscat.catrpc.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/14 0:57
 */
public class TypeUtils {

	public static Object cast(Object origin, Class<?> type) {
		if(origin == null) return null;
		Class<?> aClass = origin.getClass();
		if(type.isAssignableFrom(aClass)) {
			return origin;
		}

		if(type.isArray()) {
			if(origin instanceof List list) {
				origin = list.toArray();
			}
			int length = Array.getLength(origin);
			Class<?> componentType = type.getComponentType();
			Object resultArray = Array.newInstance(componentType, length);
			for (int i = 0; i < length; i++) {
				if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
					Array.set(resultArray, i, Array.get(origin, i));
				} else {
					Object castObject = cast(Array.get(origin, i), componentType);
					Array.set(resultArray, i, castObject);
				}
			}
			return resultArray;
		}

		if (origin instanceof HashMap map) {
			JSONObject jsonObject = new JSONObject(map);
			return jsonObject.toJavaObject(type);
		}

		if (origin instanceof JSONObject jsonObject) {
			return jsonObject.toJavaObject(type);
		}

		if(type.equals(Integer.class) || type.equals(Integer.TYPE)) {
			return Integer.valueOf(origin.toString());
		} else if(type.equals(Long.class) || type.equals(Long.TYPE)) {
			return Long.valueOf(origin.toString());
		} else if(type.equals(Float.class) || type.equals(Float.TYPE)) {
			return Float.valueOf(origin.toString());
		} else if(type.equals(Double.class) || type.equals(Double.TYPE)) {
			return Double.valueOf(origin.toString());
		} else if(type.equals(Byte.class) || type.equals(Byte.TYPE)) {
			return Byte.valueOf(origin.toString());
		} else if(type.equals(Short.class) || type.equals(Short.TYPE)) {
			return Short.valueOf(origin.toString());
		} else if(type.equals(Character.class) || type.equals(Character.TYPE)) {
			return Character.valueOf(origin.toString().charAt(0));
		}

		return null;
	}

	public static Object castMethodResult(Method method, Object data) {
		Class<?> type = method.getReturnType();
		System.out.println("method.getReturnType() = " + type);
		if (data instanceof JSONObject jsonResult) {
			if (Map.class.isAssignableFrom(type)) {
				Map resultMap = new HashMap();
				Type genericReturnType = method.getGenericReturnType();
				System.out.println(genericReturnType);
				if (genericReturnType instanceof ParameterizedType parameterizedType) {
					Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
					Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
					System.out.println("keyType  : " + keyType);
					System.out.println("valueType: " + valueType);
					jsonResult.entrySet().stream().forEach(
							e -> {
								Object key = cast(e.getKey(), keyType);
								Object value = cast(e.getValue(), valueType);
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
					if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
						Array.set(resultArray, i, array[i]);
					} else {
						Object castObject = cast(array[i], componentType);
						Array.set(resultArray, i, castObject);
					}
				}
				return resultArray;
			} else if (List.class.isAssignableFrom(type)) {
				List<Object> resultList = new ArrayList<>(array.length);
				Type genericReturnType = method.getGenericReturnType();
				System.out.println(genericReturnType);
				if (genericReturnType instanceof ParameterizedType parameterizedType) {
					Type actualType = parameterizedType.getActualTypeArguments()[0];
					System.out.println(actualType);
					for (Object o : array) {
						resultList.add(cast(o, (Class<?>) actualType));
					}
				} else {
					resultList.addAll(Arrays.asList(array));
				}
				return resultList;
			} else {
				return null;
			}
		} else {
			return cast(data, type);
		}
	}

}
