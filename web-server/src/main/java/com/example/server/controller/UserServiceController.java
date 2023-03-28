package com.example.server.controller;

import com.example.client.api.User;
import com.example.client.api.UserService;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserServiceController
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@RestController
public class UserServiceController implements UserService {

    @Override
    public Boolean register(User user) {
        return Boolean.TRUE;
    }

    @Override
    public User getUser() {
        return new User("zcatch");
    }
}
