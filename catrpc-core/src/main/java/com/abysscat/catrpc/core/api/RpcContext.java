package com.abysscat.catrpc.core.api;

import com.abysscat.catrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 20:59
 */
@Data
public class RpcContext {

	Router<InstanceMeta> router;

	LoadBalancer<InstanceMeta> loadBalancer;

	List<Filter> filters;

	private Map<String, String> parameters = new HashMap<>();

}
