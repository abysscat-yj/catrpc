package com.abysscat.catrpc.core.api;

import com.abysscat.catrpc.core.api.exception.RpcException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 1:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {

    boolean status;

    T data;

    RpcException ex;

}
