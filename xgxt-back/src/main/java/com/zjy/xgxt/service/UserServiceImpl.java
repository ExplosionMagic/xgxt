package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.mapper.UserMapper;
import com.zjy.xgxt.service.UserService;
import com.zjy.xgxt.utils.JwtUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Map<String, Object> login(User user) {
        // 1. 构造查询条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());

        // 2. 查询用户
        User loginUser = this.getOne(wrapper); // MyBatis-Plus 提供的方法

        // 3. 业务校验（密码比对）
        // 注意：生产环境应使用 BCryptPasswordEncoder 进行加密比对
        if (loginUser == null || !loginUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误"); // 抛出异常，由全局异常处理器捕获
        }

        // 4. 生成Token
        String token = JwtUtils.generateToken(loginUser.getUsername());

        // 5. 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", loginUser.getRole());
        result.put("nickName", loginUser.getNickName());

        return result;
    }
}
