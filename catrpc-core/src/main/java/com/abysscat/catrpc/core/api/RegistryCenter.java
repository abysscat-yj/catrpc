package com.abysscat.catrpc.core.api;

import com.abysscat.catrpc.core.registry.ChangedListener;

import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/17 21:20
 */
public interface RegistryCenter {

	void start();

	void stop();


	// provider侧
	void register(String service, String instance);
	void unregister(String service, String instance);

	// consumer侧
	List<String> fetchAll(String service);
	 void subscribe(String service, ChangedListener listener);

	class StaticRegistryCenter implements RegistryCenter {

		List<String> providers;

		public StaticRegistryCenter(List<String> providers) {
			this.providers = providers;
		}

		@Override
		public void start() {

		}

		@Override
		public void stop() {

		}

		@Override
		public void register(String service, String instance) {

		}

		@Override
		public void unregister(String service, String instance) {

		}

		@Override
		public List<String> fetchAll(String service) {
			return providers;
		}

		@Override
		public void subscribe(String service, ChangedListener listener) {

		}
	}

}
