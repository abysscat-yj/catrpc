package com.abysscat.catrpc.core.api;

import lombok.Data;
import lombok.ToString;

/**
 * RPC请求封装类
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 1:07
 */
@Data
@ToString
public class RpcRequest {

    private String service;

    private String methodSign;

    private Object[] args;

}
