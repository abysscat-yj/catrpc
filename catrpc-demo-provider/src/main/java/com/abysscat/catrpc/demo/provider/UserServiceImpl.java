package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.demo.api.User;
import com.abysscat.catrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 0:27
 */
@Component
@CatProvider
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "cat-" +  environment.getProperty("server.port") + "-" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "cat-" + name + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public int getId(User user) {
        if (user == null) {
            return 0;
        }
        return user.getId();
    }

    @Override
    public int[] getIds() {
        return new int[]{111, 222, 333};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{111, 222};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public List<Integer> getIdList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        return userList.stream().map(User::getId).toList();
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

    @Override
    public User[] getUsers(User[] users) {
        return users;
    }
}
