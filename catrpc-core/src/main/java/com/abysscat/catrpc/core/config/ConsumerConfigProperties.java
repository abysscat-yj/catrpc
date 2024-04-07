package com.abysscat.catrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer Config Properties
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/7 16:29
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "catrpc.consumer")
public class ConsumerConfigProperties {

	// for ha and governance
	private int retries = 1;

	private int timeout = 1000;

	private int faultLimit = 10;

	private int halfOpenInitialDelay = 10_000;

	private int halfOpenDelay = 60_000;

	private int grayRatio = 0;

}
