package com.zjy.xgxt.controller;

import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    @Autowired
    private UserService userService; // 注入接口，而不是实现类

    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        // 逻辑全部移交 Service，Controller 只需要包装 Result
        return Result.success(userService.login(user));
    }
}
