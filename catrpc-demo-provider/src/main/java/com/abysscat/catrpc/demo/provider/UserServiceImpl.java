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
}
