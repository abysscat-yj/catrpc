package com.abysscat.catrpc.core.api;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

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

    // 跨调用方需要传递的参数（也可以放到http header里）
    private Map<String,String> params = new HashMap<>();

}
