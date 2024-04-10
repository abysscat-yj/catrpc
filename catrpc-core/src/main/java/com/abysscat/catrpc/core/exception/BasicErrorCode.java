package com.abysscat.catrpc.core.exception;

/**
 * 统一错误码对象
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/28 1:21
 */
public interface BasicErrorCode {

	/**
	 *  类型
	 */
	String getType();

	/**
	 * 错误码
	 */
	String getCode();

	/**
	 * 错误信息
	 */
	String getMsg();

}
