package com.abysscat.catrpc.core.utils;

import lombok.SneakyThrows;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock Utils
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/25 0:27
 */
public class MockUtils {

	public static Object mock(Class type) {
		if(type.equals(Integer.class) || type.equals(Integer.TYPE)) {
			return 1;
		}
		if(type.equals(Long.class) || type.equals(Long.TYPE)) {
			return 10000L;
		}
		if(Number.class.isAssignableFrom(type)) {
			return 1;
		}
		if(type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
			return false;
		}
		if (type.isArray()) {
			return Array.newInstance(type.getComponentType(), 1);
		}
		if (List.class.isAssignableFrom(type)) {
			return new ArrayList<>();
		}
		if (Map.class.isAssignableFrom(type)) {
			return new HashMap<>();
		}
		if(type.equals(String.class)) {
			return "this_is_a_mock_string";
		}

		return mockPojo(type);
	}

	@SneakyThrows
	private static Object mockPojo(Class type) {
		Constructor constructor = type.getDeclaredConstructor();
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}
		Object result = constructor.newInstance();
		Field[] fields = type.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			Class<?> fType = f.getType();
			Object fValue = mock(fType);
			f.set(result, fValue);
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(mock(UserDto.class));
	}

	public static class UserDto{
		private int a;
		private String b;

		@Override
		public String toString() {
			return a + "," + b;
		}
	}

}
