package com.abysscat.catrpc.core.registry.zk;

import com.abysscat.catrpc.core.api.RegistryCenter;
import com.abysscat.catrpc.core.api.exception.ErrorEnum;
import com.abysscat.catrpc.core.api.exception.RpcException;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.registry.ChangedListener;
import com.abysscat.catrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
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
	private List<TreeCache> caches = new ArrayList<>();

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
		log.info(" =======> zk tree cache closed.");
		caches.forEach(TreeCache::close);
		client.close();
		log.info("=======> zk client stopped.");
	}

	@Override
	public void register(ServiceMeta service, InstanceMeta instance) {
		String servicePath = "/" + service.toPath();
		try {
			// 创建服务持久化节点
			if (client.checkExists().forPath(servicePath) == null) {
				client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
				log.info("=======> service register to zk: path: {}, meta: {}", servicePath, service.toMetas());
			}
			// 创建实例临时节点
			String instancePath = servicePath + "/" + instance.toPath();
			client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
			log.info("=======> instance register to zk: path: {}, meta: {}", instancePath, instance.toMetas());
		} catch (Exception e) {
			throw new RpcException(e, ErrorEnum.REGISTRY_ZK_ERROR);
		}
	}

	@Override
	public void unregister(ServiceMeta service, InstanceMeta instance) {
		String servicePath = "/" + service.toPath();
		try {
			if (client.checkExists().forPath(servicePath) == null) {
				return;
			}
			// 删除实例节点
			String instancePath = servicePath + "/" + instance.toPath();
			client.delete().quietly().forPath(instancePath);
			log.info("=======> instance unregister to zk: " + instancePath);
		} catch (Exception e) {
			throw new RpcException(e, ErrorEnum.REGISTRY_ZK_ERROR);
		}
	}

	@Override
	public List<InstanceMeta> fetchAll(ServiceMeta service) {
		String servicePath = "/" + service.toPath();
		try {
			List<String> nodes = client.getChildren().forPath(servicePath);
			return mapInstanceMetas(nodes, servicePath);
		} catch (Exception e) {
			throw new RpcException(e, ErrorEnum.REGISTRY_ZK_ERROR);
		}
	}

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
		try {
			cache.start();
		} catch (Exception e) {
			throw new RpcException(e, ErrorEnum.REGISTRY_ZK_ERROR);
		}
		caches.add(cache);
	}

	private List<InstanceMeta> mapInstanceMetas(List<String> nodes, String servicePath) {
		return nodes.stream().map(node -> {
			String[] splits = node.split("_");
			InstanceMeta instance = InstanceMeta.http(splits[0], Integer.valueOf(splits[1]));

			// 从zk拿到meta参数并赋值
			String nodePath = servicePath + "/" + node;
			byte[] bytes;
			try {
				bytes = client.getData().forPath(nodePath);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			HashMap<String, String> metaMap = JSON.parseObject(new String(bytes), HashMap.class);
			instance.setParameters(metaMap);
			return instance;
		}).collect(Collectors.toList());
	}
}
