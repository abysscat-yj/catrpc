package com.abysscat.catrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 实例描述元数据
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/21 1:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

	private String scheme;
	private String host;
	private Integer port;
	private String context;

	private boolean status; // online or offline
	private Map<String, String> parameters = new HashMap<>();

	public InstanceMeta(String scheme, String host, Integer port, String context) {
		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.context = context;
	}

	public String toPath() {
		return String.format("%s_%d", host, port);
	}

	public String toUrl() {
		return String.format("%s://%s:%d/%s", scheme, host, port, context);
	}

	public String toMetas() {
		return JSON.toJSONString(this.parameters);
	}

	public InstanceMeta addParams(Map<String, String> params) {
		this.getParameters().putAll(params);
		return this;
	}

	public static InstanceMeta http(String host, Integer port) {
		return new InstanceMeta("http", host, port, "catrpc");
	}

}
