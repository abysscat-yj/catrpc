package com.abysscat.catrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务描述元数据
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 2:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMeta {

	private String app;
	private String namespace;
	private String env;
	private String name;
	private String version;

	public String toPath() {
		return String.format("%s_%s_%s_%s_%s", app, namespace, env, name, version);
	}

}
