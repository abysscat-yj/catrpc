package com.abysscat.catrpc.demo.consumer;

import com.abysscat.catrpc.core.annotation.CatConsumer;
import com.abysscat.catrpc.core.consumer.ConsumerConfig;
import com.abysscat.catrpc.demo.api.Order;
import com.abysscat.catrpc.demo.api.OrderService;
import com.abysscat.catrpc.demo.api.User;
import com.abysscat.catrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/10 22:33
 */
@SpringBootApplication
@Import({ConsumerConfig.class})
public class CatrpcDemoConsumerApplication {

	@CatConsumer
	UserService userService;

	@CatConsumer
	OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(CatrpcDemoConsumerApplication.class, args);
	}

	@Bean
	ApplicationRunner consumerRun() {
		return x -> {
			User user = userService.findById(1);
			System.out.println("RPC Result: userService.findById(1): " + user);

			Order order = orderService.findById(1);
			System.out.println("RPC Result: orderService.findById(1):" + order);

//			Order order404 = orderService.findById(404);
//			System.out.println("RPC Result: orderService.findById(404):" + order404);

			int id = orderService.getId(1);
			System.out.println("RPC Result: orderService.getId(1):" + id);
		};
	}
}
