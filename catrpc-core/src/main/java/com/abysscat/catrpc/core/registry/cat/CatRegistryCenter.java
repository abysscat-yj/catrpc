package com.abysscat.catrpc.core.registry.cat;

import com.abysscat.catrpc.core.consumer.HttpInvoker;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.registry.ChangedListener;
import com.abysscat.catrpc.core.registry.Event;
import com.abysscat.catrpc.core.registry.RegistryCenter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CatRegistryCenter implementation.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/25 1:22
 */
@Slf4j
public class CatRegistryCenter implements RegistryCenter {

	/**
	 * 注册中心服务端接口地址
	 */
	private static final String REG_API_PATH = "/reg";
	private static final String UNREG_API_PATH = "/unreg";
	private static final String FINDALL_API_PATH = "/findAll";
	private static final String VERSION_API_PATH = "/version";
	private static final String RENEWS_API_PATH = "/renews";

	@Value(value = "${catregistry.servers}")
	private String servers;

	/**
	 * 存放本地服务版本号， 用于远程服务实例信息变更时，通知订阅方
	 * key: 服务名
	 * value: 服务实例信息版本号
	 */
	private static final Map<String, Long> VERSIONS = new HashMap<>();

	/**
	 * 用来存放本地注册的服务实例信息， 用于定时向远程注册中心 server 端发送心跳
	 * key: 实例对象
	 * value: 服务对象
	 */
	private static final MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();

	/**
	 * 注册中心定时任务调度器， 处理健康检查、服务发现等任务
	 */
	CatRegistryScheduler scheduler = new CatRegistryScheduler();

	@Override
	public void start() {
		log.info("start catregistry server :{}", servers);

		scheduler.start();
		scheduler.providerSchedule(this::heartbeat);
	}

	@Override
	public void stop() {
		log.info("stop catregistry server :{}", servers);
		scheduler.stop();
	}

	/**
	 * 向远程注册中心 server 端发送心跳
	 */
	public void heartbeat() {
		RENEWS.keySet().forEach(
				instance -> {
					StringBuffer stringBuffer = new StringBuffer();
					List<ServiceMeta> serviceMetas = RENEWS.get(instance);
					for (ServiceMeta service : serviceMetas) {
						stringBuffer.append(service.toPath()).append(",");
					}
					String services = stringBuffer.toString();
					if (services.endsWith(",")) {
						services = services.substring(0, services.length() - 1);
					}
					Long timestamp = null;
					try {
						timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), renewsPath(serviceMetas), Long.class);
					} catch (Exception e) {
						log.error("renew instance {} for {} at {} failed", instance, services, timestamp);
						return;
					}
					log.info("renew instance {} for {} at {} succeed", instance, services, timestamp);
				}
		);
	}


	@Override
	public void register(ServiceMeta service, InstanceMeta instance) {
		log.info("register instance [{}] for service :{}", instance, service);
		HttpInvoker.httpPost(JSON.toJSONString(instance), regPath(service), InstanceMeta.class);
		log.info("registered instance [{}] for service :{}", instance, service);
		RENEWS.add(instance, service);
	}

	@Override
	public void unregister(ServiceMeta service, InstanceMeta instance) {
		log.info("unregister instance [{}] for service :{}", instance, service);
		HttpInvoker.httpPost(JSON.toJSONString(instance), unregPath(service), InstanceMeta.class);
		log.info("unregistered instance [{}] for service :{}", instance, service);
		RENEWS.remove(instance, service);
	}

	@Override
	public List<InstanceMeta> fetchAll(ServiceMeta service) {
		log.info("fetchAll instances for service :{}", service);
		List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
		});
		log.info("fetchAll instances :{}", instances);
		return instances;
	}

	@Override
	public void subscribe(ServiceMeta service, ChangedListener listener) {
		// 定时比对本地和远程版本，实现服务发现
		scheduler.consumerSchedule(() -> {
			Long localVersion = VERSIONS.getOrDefault(service.toPath(), -1L);
			Long remoteVersion = HttpInvoker.httpGet(versionPath(service), Long.class);

			log.info("subscribe get localVersion = {}, remoteVersion = {}", localVersion, remoteVersion);

			// 对比本地和远程版本号， 判断是否更新
			if (localVersion < remoteVersion) {
				List<InstanceMeta> instances = fetchAll(service);
				listener.fire(new Event(instances));
				// 只有在刷新列表并通知完成后，才更新本地版本号
				VERSIONS.put(service.toPath(), remoteVersion);
			}
		});
	}

	private String regPath(ServiceMeta service) {
		return path(REG_API_PATH, service);
	}

	private String unregPath(ServiceMeta service) {
		return path(UNREG_API_PATH, service);
	}

	private String findAllPath(ServiceMeta service) {
		return path(FINDALL_API_PATH, service);
	}

	private String versionPath(ServiceMeta service) {
		return path(VERSION_API_PATH, service);
	}

	private String path(String apiPath, ServiceMeta service) {
		return servers + apiPath + "?service=" + service.toPath();
	}

	private String renewsPath(List<ServiceMeta> serviceList) {
		return path(RENEWS_API_PATH, serviceList);
	}

	private String path(String apiPath, List<ServiceMeta> serviceList) {
		StringBuffer sb = new StringBuffer();
		for (ServiceMeta service : serviceList) {
			sb.append(service.toPath()).append(",");
		}
		String services = sb.toString();
		if(services.endsWith(",")) {
			services = services.substring(0, services.length() - 1);
		}
		return servers + apiPath + "?services=" + services;
	}
}
