package com.abysscat.catrpc.core.exception;

import lombok.Getter;

/**
 * 错误枚举类
 * <p>
 * X => 技术类异常：
 * Y => 业务类异常：
 * Z => unknown, 搞不清楚，再归类到X或Y
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/28 1:21
 */
@Getter
public enum ErrorEnum implements BasicErrorCode {

	SOCKET_TIMEOUT_ERROR("X", "001", "http_invoke_timeout"),
	NO_SUCH_METHOD_ERROR("X", "002", "method_not_exists"),
	REGISTRY_ZK_ERROR("X", "003", "registry_zk_error"),
	EXCEED_LIMIT_ERROR("X", "004", "tps_exceed_limit"),

	UNKNOWN_ERROR("Z", "001", "unknown");

	private final String type;

	private final String code;

	private final String msg;

	ErrorEnum(String type, String code, String msg) {
		this.type = type;
		this.code = code;
		this.msg = msg;
	}
}
