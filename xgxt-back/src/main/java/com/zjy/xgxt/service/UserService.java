package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.xgxt.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> login(User user);
}
