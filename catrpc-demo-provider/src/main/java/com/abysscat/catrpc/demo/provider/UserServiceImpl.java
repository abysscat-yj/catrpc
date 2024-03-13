package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.annotation.CatProvider;
import com.abysscat.catrpc.demo.api.User;
import com.abysscat.catrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 0:27
 */
@Component
@CatProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "cat-" + System.currentTimeMillis());
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
}
