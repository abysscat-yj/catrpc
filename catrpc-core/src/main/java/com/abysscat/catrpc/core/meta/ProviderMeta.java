package com.abysscat.catrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/14 1:05
 */
@Data
public class ProviderMeta {

	Method method;

	String methodSign;

	Object serviceImpl;

}
