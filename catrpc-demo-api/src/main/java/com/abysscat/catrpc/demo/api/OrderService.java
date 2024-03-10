package com.abysscat.catrpc.demo.api;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 23:31
 */
public interface OrderService {

	Order findById(Integer id);

	int getId(int id);

}
