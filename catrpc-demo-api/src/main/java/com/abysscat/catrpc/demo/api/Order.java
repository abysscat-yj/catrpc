package com.abysscat.catrpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 23:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	Long id;

	Float amount;

}
