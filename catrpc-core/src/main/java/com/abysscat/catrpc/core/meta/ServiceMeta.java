package com.abysscat.catrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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

	private Map<String, String> parameters = new HashMap<>(); // 额外元数据参数，比如服务灰度比例

	public String toPath() {
		return String.format("%s_%s_%s_%s_%s", app, namespace, env, name, version);
	}

	public String toMetas() {
		return JSON.toJSONString(this.parameters);
	}

}
