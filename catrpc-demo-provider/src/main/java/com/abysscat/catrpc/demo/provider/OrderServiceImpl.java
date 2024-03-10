package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.demo.api.Order;
import com.abysscat.catrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 23:33
 */
@CatProvider
@Component
public class OrderServiceImpl implements OrderService {
	@Override
	public Order findById(Integer id) {

		if(id == 404) {
			throw new RuntimeException("404 exception");
		}

		return new Order(id.longValue(), 12.34f);
	}

	@Override
	public int getId(int id) {
		return id;
	}
}
