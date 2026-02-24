package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.zjy.xgxt.utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin // 允许跨域
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 1. 统一登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> form) {
        String account = form.get("account");
        String password = form.get("password");

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            return Result.error("账号或密码不能为空");
        }

        // 1. 根据 学号/教工ID 或者 手机号 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUserNo, account)
                .or()
                .eq(User::getPhone, account));

        // 【加上这一行！】强制要求数据库只返回一条匹配的数据，防止测试产生的数据重复导致 500 报错
        wrapper.last("LIMIT 1");

        User user = userService.getOne(wrapper);

        // 2. 账号存在性校验
        if (user == null) {
            return Result.error("账号不存在，请检查您的输入");
        }

        // 3. 密码校验 (同时兼容我们刚加的 MD5 密文 和 之前测试阶段创建的明文密码)
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword()) && !md5Password.equals(user.getPassword())) {
            return Result.error("账号或密码错误");
        }

        // 4. 账号状态校验 (status = 0 代表被管理员停用)
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被管理员停用，禁止登录，请联系教务处");
        }

        // 5. 登录成功，构建返回给前端的本地存储信息 (严格抹除密码等敏感信息)
        Map<String, Object> resData = new HashMap<>();
        resData.put("id", user.getId());
        resData.put("userNo", user.getUserNo());
        resData.put("name", user.getName());
        resData.put("role", user.getRole());
        resData.put("auditStatus", user.getAuditStatus()); // 把学籍状态也传给前端
        resData.put("token", JwtUtils.generateToken(user.getUserNo()));

        return Result.success(resData);
    }

    /**
     * 2. 统一注册接口
     * 实际的业务逻辑（自增学号、MD5密码加密、默认状态设置）都在 UserServiceImpl 的 register 方法中
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            // 捕获我们在 Service 层抛出的异常 (如：手机号重复、必填项为空等)
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统异常，注册失败");
        }
    }
}