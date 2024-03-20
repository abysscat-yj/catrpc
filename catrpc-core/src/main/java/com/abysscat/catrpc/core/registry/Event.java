package com.abysscat.catrpc.core.registry;

import com.abysscat.catrpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/19 1:01
 */
@Data
@AllArgsConstructor
public class Event {

	List<InstanceMeta> data;

}
