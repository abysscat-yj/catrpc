package com.abysscat.catrpc.core.exception;

import lombok.Data;

/**
 * RPC异常类
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/28 0:51
 */
@Data
public class RpcException extends RuntimeException {

	private String type;

	private String code;

	public RpcException() {
	}

	public RpcException(BasicErrorCode errorCode) {
		super(errorCode.getMsg());
		this.type = errorCode.getType();
		this.code = errorCode.getCode();
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(String message, ErrorEnum errorEnum) {
		super(message);
		this.code = errorEnum.getCode();
	}

	public RpcException(Throwable cause) {
		super(cause);
	}

	public RpcException(Throwable cause, ErrorEnum errorEnum) {
		super(cause);
		this.type = errorEnum.getType();
		this.code = errorEnum.getCode();
	}

	public RpcException(Throwable cause, String type, String code) {
		super(cause);
		this.type = type;
		this.code = code;
	}

}
