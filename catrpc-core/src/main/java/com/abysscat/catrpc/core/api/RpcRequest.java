package com.abysscat.catrpc.core.api;

import lombok.Data;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 1:07
 */
@Data
public class RpcRequest {

    private String service;

    private String methodSign;

    private Object[] args;

}
