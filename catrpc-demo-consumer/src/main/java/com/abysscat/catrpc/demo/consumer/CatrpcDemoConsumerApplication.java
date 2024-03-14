package com.abysscat.catrpc.demo.consumer;

import com.abysscat.catrpc.core.annotation.CatConsumer;
import com.abysscat.catrpc.core.consumer.ConsumerConfig;
import com.abysscat.catrpc.demo.api.OrderService;
import com.abysscat.catrpc.demo.api.User;
import com.abysscat.catrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

			User user2 = userService.findById(1, "yj");
			System.out.println("RPC Result: userService.findById(1, \"yj\"): " + user2);

			long userId = userService.getId(11);
			System.out.println("RPC Result: userService.getId(11): " + userId);

			int userId2 = userService.getId(new User(22, ""));
			System.out.println("RPC Result: userService.getId(new User(22, \"\")): " + userId2);

			int[] ids = userService.getIds();
			System.out.println("RPC Result: userService.getIds(): " + Arrays.toString(ids));

			int[] ids2 = userService.getIds(new int[]{333, 555});
			System.out.println("RPC Result: userService.getIds(new int[]{333, 555}): " + Arrays.toString(ids2));

			long[] longIds = userService.getLongIds();
			System.out.println("RPC Result: userService.getLongIds(): " + Arrays.toString(longIds));

			List<User> list = userService.getList(Arrays.asList(new User(1, "yy"), new User(2, "jj")));
			System.out.println("RPC Result: userService.getList(Arrays.asList(new User(1, \"yy\"), new User(2, \"jj\"))): " + list);

			List<User> list2 = userService.getList(null);
			System.out.println("RPC Result: userService.getList(null): " + list2);

			List<Integer> idList = userService.getIdList(Arrays.asList(new User(11, ""), new User(22, "")));
			System.out.println("RPC Result: userService.getIdList(Arrays.asList(new User(11, \"\"), new User(22, \"\")): " + idList);

			Map<String, User> map = userService.getMap(Map.of("cc", new User(1, "111")));
			System.out.println("RPC Result: userService.getMap(Map.of(\"cc\", new User(1, \"111\"))): " + map);

//			Order order = orderService.findById(1);
//			System.out.println("RPC Result: orderService.findById(1):" + order);

//			Order order404 = orderService.findById(404);
//			System.out.println("RPC Result: orderService.findById(404):" + order404);

//			int id = orderService.getId(1);
//			System.out.println("RPC Result: orderService.getId(1):" + id);
		};
	}
}
