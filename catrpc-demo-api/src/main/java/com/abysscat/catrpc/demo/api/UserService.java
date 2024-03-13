package com.abysscat.catrpc.demo.api;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 0:12
 */
public interface UserService {

    User findById(int id);

    User findById(int id, String name);

    long getId(long id);

    int getId(User user);

    int[] getIds();
    long[] getLongIds();
    int[] getIds(int[] ids);

}
