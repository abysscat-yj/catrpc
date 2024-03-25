package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.demo.api.User;
import com.abysscat.catrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
        return new User(id, "cat-"
                + environment.getProperty("server.port")
                + "_" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "cat-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "cat123";
    }

    @Override
    public String getName(int id) {
        return "cat-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[] {100,200,300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1,2,3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User findById(long id) {
        return new User(Long.valueOf(id).intValue(), "cat");
    }

    @Override
    public User ex(boolean flag) {
        if(flag) throw new RuntimeException("just throw an exception");
        return new User(100, "cat100");
    }
}
