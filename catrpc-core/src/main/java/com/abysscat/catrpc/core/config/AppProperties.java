package com.abysscat.catrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * App Config Properties
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/7 16:28
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "catrpc.app")
public class AppProperties {

	// for app instance
	private String id = "app1";

	private String namespace = "public";

	private String env = "dev";

	private String version = "v1";

}
