package com.abysscat.catrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider Config Properties
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/7 22:05
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "catrpc.provider")
public class ProviderProperties {

	// for provider
	Map<String, String> metas = new HashMap<>();


}
