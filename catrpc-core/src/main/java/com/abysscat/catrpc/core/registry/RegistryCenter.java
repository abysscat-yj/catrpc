package com.abysscat.catrpc.core.api;

import com.abysscat.catrpc.core.meta.InstanceMeta;
import com.abysscat.catrpc.core.meta.ServiceMeta;
import com.abysscat.catrpc.core.registry.ChangedListener;

import java.util.List;

/**
 * 注册中心接口
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 21:20
 */
public interface RegistryCenter {

	void start();

	void stop();


	// provider侧
	void register(ServiceMeta service, InstanceMeta instance);
	void unregister(ServiceMeta service, InstanceMeta instance);

	// consumer侧
	List<InstanceMeta> fetchAll(ServiceMeta service);
	 void subscribe(ServiceMeta service, ChangedListener listener);

	class StaticRegistryCenter implements RegistryCenter {

		List<InstanceMeta> providers;

		public StaticRegistryCenter(List<InstanceMeta> providers) {
			this.providers = providers;
		}

		@Override
		public void start() {

		}

		@Override
		public void stop() {

		}

		@Override
		public void register(ServiceMeta service, InstanceMeta instance) {

		}

		@Override
		public void unregister(ServiceMeta service, InstanceMeta instance) {

		}

		@Override
		public List<InstanceMeta> fetchAll(ServiceMeta service) {
			return providers;
		}

		@Override
		public void subscribe(ServiceMeta service, ChangedListener listener) {

		}
	}

}
