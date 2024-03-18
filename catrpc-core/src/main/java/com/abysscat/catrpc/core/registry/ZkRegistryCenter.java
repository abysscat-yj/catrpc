package com.abysscat.catrpc.core.registry;

import com.abysscat.catrpc.core.api.RegistryCenter;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 21:44
 */
public class ZkRegistryCenter implements RegistryCenter {

	private CuratorFramework client = null;

	@Override
	public void start() {
		RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder()
				.connectString("localhost:2181")
				.namespace("catrpc")
				.retryPolicy(policy)
				.build();
		client.start();
		System.out.println("=======> zk client started.");
	}

	@Override
	public void stop() {
		client.close();
		System.out.println("=======> zk client stopped.");
	}

	@SneakyThrows
	@Override
	public void register(String service, String instance) {
		String servicePath = "/" + service;
		// 创建服务持久化节点
		if (client.checkExists().forPath(servicePath) == null) {
			client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
			System.out.println("=======> service register to zk: " + servicePath);
		}
		// 创建实例临时节点
		String instancePath = servicePath + "/" + instance;
		client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
		System.out.println("=======> instance register to zk: " + instancePath);
	}

	@SneakyThrows
	@Override
	public void unregister(String service, String instance) {
		String servicePath = "/" + service;
		if (client.checkExists().forPath(servicePath) == null) {
			return;
		}
		// 删除实例节点
		String instancePath = servicePath + "/" + instance;
		client.delete().quietly().forPath(instancePath);
		System.out.println("=======> instance unregister to zk: " + instancePath);
	}

	@SneakyThrows
	@Override
	public List<String> fetchAll(String service) {
		String servicePath = "/" + service;
		return client.getChildren().forPath(servicePath);
	}

	@SneakyThrows
	@Override
	public void subscribe(String service, ChangedListener listener) {
		final TreeCache cache = TreeCache.newBuilder(client, "/"+service)
				.setCacheData(true)
				.setMaxDepth(2)
				.build();
		cache.getListenable().addListener(
				(curator, event) -> {
					// 有任何节点变动这里会执行
					System.out.println("zk subscribe event: " + event);
					List<String> nodes = fetchAll(service);
					listener.fire(new Event(nodes));
				}
		);
		cache.start();
	}
}
