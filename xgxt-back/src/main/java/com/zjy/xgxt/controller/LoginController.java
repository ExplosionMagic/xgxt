package com.zjy.xgxt.controller;

import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    @Autowired
    private UserService userService; // 注入接口，而不是实现类

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginData) {
        String account = loginData.get("account"); // 前端传 account
        String password = loginData.get("password");

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            return Result.error("账号和密码不能为空");
        }

        User user = new User();
        user.setUserNo(account); // 将接收到的账号借用 userNo 字段传给 Service
        user.setPassword(password);

        return Result.success(userService.login(user));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功");
    }
}
