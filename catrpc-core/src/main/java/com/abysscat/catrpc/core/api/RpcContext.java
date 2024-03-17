package com.abysscat.catrpc.core.api;

import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 20:59
 */
@Data
public class RpcContext {

	Router router;

	LoadBalancer loadBalancer;

	List<Filter> filters;

}
