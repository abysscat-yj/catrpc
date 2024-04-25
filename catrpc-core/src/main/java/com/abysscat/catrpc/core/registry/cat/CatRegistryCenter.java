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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CatRegistryCenter implementation.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/25 1:22
 */
@Slf4j
public class CatRegistryCenter implements RegistryCenter {

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
	private ScheduledExecutorService consumerExecutor = null;
	private ScheduledExecutorService producerExecutor = null;

	@Override
	public void start() {
		log.info("start catregistry server :{}", servers);
		consumerExecutor = Executors.newScheduledThreadPool(1);
		producerExecutor = Executors.newScheduledThreadPool(1);

		// 定时向远程注册中心 server 端发送心跳
		producerExecutor.scheduleAtFixedRate(() -> {
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
							timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/renews?services=" + services, Long.class);
						} catch (Exception e) {
							log.error("renew instance {} for {} at {} failed", instance, services, timestamp);
							return;
						}
						log.info("renew instance {} for {} at {} succeed", instance, services, timestamp);
					}
			);
		}, 5, 5, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		log.info("stop catregistry server :{}", servers);
		gracefulShutdown(consumerExecutor);
		gracefulShutdown(producerExecutor);
	}

	private void gracefulShutdown(ScheduledExecutorService executorService) {
		executorService.shutdown();
		try {
			// 延迟关闭，给线程池执行中任务收尾时间
			executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
			if (!executorService.isTerminated()) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public void register(ServiceMeta service, InstanceMeta instance) {
		log.info("register instance [{}] for service :{}", instance, service);
		HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/reg?service=" + service.toPath(), InstanceMeta.class);
		log.info("registered instance [{}] for service :{}", instance, service);
		RENEWS.add(instance, service);
	}

	@Override
	public void unregister(ServiceMeta service, InstanceMeta instance) {
		log.info("unregister instance [{}] for service :{}", instance, service);
		HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unreg?service=" + service.toPath(), InstanceMeta.class);
		log.info("unregistered instance [{}] for service :{}", instance, service);
		RENEWS.remove(instance, service);
	}

	@Override
	public List<InstanceMeta> fetchAll(ServiceMeta service) {
		log.info("fetchAll instances for service :{}", service);
		List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(),
				new TypeReference<List<InstanceMeta>>() {
				});
		log.info("fetchAll instances :{}", instances);
		return instances;
	}

	@Override
	public void subscribe(ServiceMeta service, ChangedListener listener) {
		consumerExecutor.scheduleWithFixedDelay(() -> {
			Long localVersion = VERSIONS.getOrDefault(service.toPath(), -1L);
			Long remoteVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);

			log.info("subscribe get localVersion = {}, remoteVersion = {}", localVersion, remoteVersion);

			// 对比本地和远程版本号， 判断是否更新
			if (localVersion < remoteVersion) {
				List<InstanceMeta> instances = fetchAll(service);
				listener.fire(new Event(instances));
				// 只有在刷新列表并通知完成后，才更新本地版本号
				VERSIONS.put(service.toPath(), remoteVersion);
			}
		}, 1, 5, TimeUnit.SECONDS);
	}
}
