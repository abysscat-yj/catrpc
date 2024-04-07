package com.abysscat.catrpc.demo.consumer;

import com.abysscat.catrpc.core.test.TestZKServer;
import com.abysscat.catrpc.demo.provider.CatrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/26 0:01
 */
@SpringBootTest(classes = {CatrpcDemoConsumerApplication.class})
class CatrpcDemoConsumerApplicationTest {

	static ApplicationContext context;

	static TestZKServer zkServer = new TestZKServer();

	@BeforeAll
	static void init() {
		zkServer.start();
		// 先启动 provider
		context = SpringApplication.run(CatrpcDemoProviderApplication.class,
				"--server.port=8094",
				"--catrpc.zk.server=localhost:2182",
				"--logging.level.com.abysscat.catrpc=debug"
		);
	}

	@Test
	void contextLoad() {
		System.out.println("======> CatrpcDemoConsumerApplicationTest ing...");
	}

	@AfterAll
	static void destroy() {
		SpringApplication.exit(context, () -> 1);
		zkServer.stop();
	}

}