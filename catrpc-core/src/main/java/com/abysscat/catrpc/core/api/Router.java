package com.abysscat.catrpc.core.api;

import java.util.List;

/**
 * 路由器
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 17:38
 */
public interface Router<T> {

	List<T> route(List<T> providers);

	Router Default = providers -> providers;

}
