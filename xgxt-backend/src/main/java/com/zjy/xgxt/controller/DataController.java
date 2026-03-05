package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class DataController {

    @Autowired
    private UserService userService;

    @GetMapping("/echarts")
    public Result<Map<String, Object>> getEchartsData() {
        // 使用 QueryWrapper 聚合查询：按专业分组，统计每个专业的人数
        // 注意：这里必须用 QueryWrapper 而不是 LambdaQueryWrapper，因为要写 SQL 聚合函数
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("major as name", "count(id) as value")
                .eq("role", "STUDENT")
                .isNotNull("major")
                .ne("major", "") // 排除专业为空的数据
                .groupBy("major");

        // listMaps 会直接返回形如 [{name: "软件工程", value: 50}, {name: "土木工程", value: 30}] 的数据，完美契合 Echarts 格式！
        List<Map<String, Object>> majorData = userService.listMaps(wrapper);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("majorData", majorData);

        return Result.success(resultMap);
    }
}