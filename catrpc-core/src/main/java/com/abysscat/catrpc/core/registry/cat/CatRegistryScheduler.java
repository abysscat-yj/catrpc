package com.abysscat.catrpc.core.registry.cat;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * registry scheduler.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/28 11:13
 */
@Slf4j
public class CatRegistryScheduler {

	/**
	 * 生产者定时任务， 通过心跳机制实现健康检查
	 */
	private ScheduledExecutorService providerExecutor = null;

	/**
	 * 消费者定时任务， 通过定时服务版本检测实现服务发现
	 */
	private ScheduledExecutorService consumerExecutor = null;


	public void start() {
		this.providerExecutor = Executors.newScheduledThreadPool(1);
		this.consumerExecutor = Executors.newScheduledThreadPool(1);
	}

	public void consumerSchedule(Callback callback) {
		consumerExecutor.scheduleAtFixedRate(() -> {
			try {
				callback.call();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}, 1, 5, TimeUnit.SECONDS);
	}

	public void providerSchedule(Callback callback) {
		providerExecutor.scheduleAtFixedRate(() -> {
			try {
				callback.call();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	public void stop() {
		gracefulShutdown(consumerExecutor);
		gracefulShutdown(providerExecutor);
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

	public interface Callback {
		void call() throws Exception;
	}
}
