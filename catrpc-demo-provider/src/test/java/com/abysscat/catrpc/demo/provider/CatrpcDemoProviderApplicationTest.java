package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.test.TestZKServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/26 1:16
 */
@SpringBootTest
class CatrpcDemoProviderApplicationTest {

	static TestZKServer zkServer = new TestZKServer();

	@BeforeAll
	static void init() {
		zkServer.start();
	}

	@Test
	void contextLoads() {
		System.out.println("======> CatrpcDemoProviderApplicationTest ing...");
	}

	@AfterAll
	static void destroy() {
		zkServer.stop();
	}

}