package com.example.client.controller;

import com.example.client.api.User;
import com.example.client.api.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * UserController
 *
 * @author <a href="mailto:zhanggaohao@trgroup.cn">张高豪</a>
 * @since 2023/3/19
 */
@RestController
public class UserController {

    private final RedisTemplate redisTemplate;
    private final UserService userService;

    public UserController(RedisTemplate redisTemplate, UserService userService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    @PostMapping("/register")
    public Boolean register() {
        User user = new User();
        return userService.register(user);
    }

    @GetMapping("/user")
    public User getUser() {
        User user;
        Object obj = redisTemplate.opsForValue().get("user");
        if (obj != null) {
            user = (User) obj;
        } else {
            user = userService.getUser();
            redisTemplate.opsForValue().set("user", user, Duration.ofMinutes(1));
        }
        return user;
    }
}
