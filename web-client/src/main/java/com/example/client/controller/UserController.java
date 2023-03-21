package com.example.client.controller;

import com.example.client.api.User;
import com.example.client.api.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Boolean register() {
        User user = new User();
        return userService.register(user);
    }

    @GetMapping("/user")
    public User getUser() {
        return userService.getUser();
    }
}
