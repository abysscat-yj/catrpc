package com.abysscat.catrpc.core.cluster;

import com.abysscat.catrpc.core.api.Router;
import com.abysscat.catrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由器
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/1 1:44
 */
@Slf4j
@Data
public class GrayRouter implements Router<InstanceMeta> {

	private int grayRatio; // todo 可以按服务粒度配置灰度比例

	private final Random random = new Random();

	public GrayRouter(int grayRatio) {
		this.grayRatio = grayRatio;
	}

	@Override
	public List<InstanceMeta> route(List<InstanceMeta> providers) {
		if (providers == null || providers.size() <= 1) {
			return providers;
		}

		List<InstanceMeta> normalNodes = new ArrayList<>();
		List<InstanceMeta> grayNodes = new ArrayList<>();

		providers.forEach(p -> {
			if ("true".equals(p.getParameters().get("gray"))) {
				grayNodes.add(p);
			} else {
				normalNodes.add(p);
			}
		});

		log.debug("grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
				grayNodes.size(), normalNodes.size(), grayRatio);

		if (normalNodes.isEmpty() || grayNodes.isEmpty()) return providers;
		if (grayRatio <= 0) {
			return normalNodes;
		} else if (grayRatio >= 100) {
			return grayNodes;
		}

		if (random.nextInt(100) < grayRatio) {
			log.debug("grayRouter grayNodes ===> {}", grayNodes);
			return grayNodes;
		} else {
			log.debug("grayRouter normalNodes ===> {}", normalNodes);
			return normalNodes;
		}
	}
}
