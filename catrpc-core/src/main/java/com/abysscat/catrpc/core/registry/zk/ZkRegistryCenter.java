package com.abysscat.catrpc.core.registry.zk;

import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.registry.ChangedListener;
import com.abysscat.catrpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 21:44
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

	private CuratorFramework client = null;

	@Value("${catrpc.zkServer}")
	private String zkServer;

	@Value("${catrpc.zkRoot}")
	private String zkRoot;

	@Override
	public void start() {
		RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder()
				.connectString(zkServer)
				.namespace(zkRoot)
				.retryPolicy(policy)
				.build();
		client.start();
		log.info("=======> zk client started. server: " + zkServer + "/" + zkRoot);
	}

	@Override
	public void stop() {
		client.close();
		log.info("=======> zk client stopped.");
	}

	@SneakyThrows
	@Override
	public void register(ServiceMeta service, InstanceMeta instance) {
		String servicePath = "/" + service.toPath();
		// 创建服务持久化节点
		if (client.checkExists().forPath(servicePath) == null) {
			client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
			log.info("=======> service register to zk: " + servicePath);
		}
		// 创建实例临时节点
		String instancePath = servicePath + "/" + instance.toPath();
		client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
		log.info("=======> instance register to zk: " + instancePath);
	}

	@SneakyThrows
	@Override
	public void unregister(ServiceMeta service, InstanceMeta instance) {
		String servicePath = "/" + service.toPath();
		if (client.checkExists().forPath(servicePath) == null) {
			return;
		}
		// 删除实例节点
		String instancePath = servicePath + "/" + instance.toPath();
		client.delete().quietly().forPath(instancePath);
		log.info("=======> instance unregister to zk: " + instancePath);
	}

	@SneakyThrows
	@Override
	public List<InstanceMeta> fetchAll(ServiceMeta service) {
		String servicePath = "/" + service.toPath();
		List<String> nodes = client.getChildren().forPath(servicePath);
		return mapInstanceMetas(nodes);
	}

	@SneakyThrows
	@Override
	public void subscribe(ServiceMeta service, ChangedListener listener) {
		final TreeCache cache = TreeCache.newBuilder(client, "/"+service.toPath())
				.setCacheData(true)
				.setMaxDepth(2)
				.build();
		cache.getListenable().addListener(
				(curator, event) -> {
					// 有任何节点变动这里会执行
					log.info("zk subscribe event: " + event);
					List<InstanceMeta> nodes = fetchAll(service);
					listener.fire(new Event(nodes));
				}
		);
		cache.start();
	}

	private static List<InstanceMeta> mapInstanceMetas(List<String> nodes) {
		return nodes.stream().map(node -> {
			String[] splits = node.split("_");
			return InstanceMeta.http(splits[0], Integer.valueOf(splits[1]));
		}).collect(Collectors.toList());
	}
}
