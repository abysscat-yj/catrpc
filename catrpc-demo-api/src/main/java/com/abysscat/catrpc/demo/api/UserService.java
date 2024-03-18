package com.abysscat.catrpc.demo.api;

import java.util.List;
import java.util.Map;

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

    List<User> getList(List<User> userList);

    List<Integer> getIdList(List<User> userList);

    Map<String, User> getMap(Map<String, User> userMap);

    User[] getUsers(User[] users);

}
