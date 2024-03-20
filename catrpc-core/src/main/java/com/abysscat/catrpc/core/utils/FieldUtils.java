package com.abysscat.catrpc.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 1:07
 */
public class FieldUtils {

	public static List<Field> findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotationClass) {
		List<Field> result = new ArrayList<>();
		while (aClass != null) {
			Field[] fields = aClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(annotationClass)) {
					result.add(field);
				}
			}
			// 由于容器里的CatrpcDemoConsumerApplication类是被SpringCGlib代理过的子类，所以默认拿不到userService field
			aClass = aClass.getSuperclass();
		}

		return result;
	}

}
