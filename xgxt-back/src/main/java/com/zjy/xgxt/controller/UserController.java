package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 1. 根据学号/教工ID获取个人详细信息 (用于个人中心)
     */
    @GetMapping("/info/{userNo}")
    public Result<User> getInfo(@PathVariable String userNo) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserNo, userNo);
        User user = userService.getOne(wrapper);
        if (user != null) {
            user.setPassword(null); // 返回给前端前抹除密码，保证安全
        }
        return Result.success(user);
    }

    /**
     * 2. 个人自己修改信息 (严格限制：只能修改手机号、联系地址、密码)
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 先从数据库查出原信息
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 如果修改了手机号，需校验唯一性
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, user.getPhone());
            if (userService.count(phoneQuery) > 0) {
                return Result.error("该手机号已被其他账号绑定");
            }
            dbUser.setPhone(user.getPhone());
        }

        // 只允许覆盖这三个字段，防止前端恶意传参修改角色或姓名
        if (StringUtils.hasText(user.getAddress())) {
            dbUser.setAddress(user.getAddress());
        }
        if (StringUtils.hasText(user.getPassword())) {
            dbUser.setPassword(user.getPassword());
        }

        userService.updateById(dbUser);
        return Result.success("个人信息保存成功");
    }

    /**
     * 3. 管理员获取所有用户列表 (带条件模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            // 支持按姓名或学号模糊查询
            wrapper.like(User::getName, name).or().like(User::getUserNo, name);
        }
        wrapper.orderByDesc(User::getId);

        List<User> list = userService.list(wrapper);
        // 批量抹除密码返回
        list.forEach(u -> u.setPassword(null));

        return Result.success(list);
    }

    /**
     * 4. 管理员高级更新 (除用户组和学号外，可修改任意信息)
     */
    @PutMapping("/admin-update")
        public Result<?> adminUpdate(@RequestBody User user) {
            if (user.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            // 必须先从数据库查出该用户原来的信息
            User dbUser = userService.getById(user.getId());
            if (dbUser == null) {
                return Result.error("用户不存在");
            }

            // 如果管理员修改了该用户的手机号，需要校验唯一性
            if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
                LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
                phoneQuery.eq(User::getPhone, user.getPhone());
                if (userService.count(phoneQuery) > 0) {
                    return Result.error("该手机号已被使用，请更换");
                }
            }

            // --- 核心限制逻辑 ---
            // 无论前端有没有传，或者传了什么，强制把“用户组”和“学号”重置为数据库原来的值
            user.setRole(dbUser.getRole());
            user.setUserNo(dbUser.getUserNo());

            // 如果密码框是空的（前端没填），则保留原密码不修改
            if (!StringUtils.hasText(user.getPassword())) {
                user.setPassword(dbUser.getPassword());
            }

            // 更新数据库
            userService.updateById(user);
            return Result.success("用户信息更新成功");
        }
        @PutMapping("/password")
            public Result<?> updatePassword(@RequestBody Map<String, String> params) {
                Integer id = Integer.parseInt(params.get("id"));
                String oldPassword = params.get("oldPassword");
                String newPassword = params.get("newPassword");

                User dbUser = userService.getById(id);
                if (dbUser == null) {
                    return Result.error("用户不存在");
                }

                // 1. 验证旧密码是否正确 (将前端传来的明文旧密码进行MD5加密，再与数据库比对)
                String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());

                // 兼容一下测试期的明文密码：如果数据库密码既不等于密文，也不等于明文，就报错
                if (!dbUser.getPassword().equals(md5OldPassword) && !dbUser.getPassword().equals(oldPassword)) {
                    return Result.error("原密码错误");
                }

                // 2. 验证新旧密码是否相同
                if (oldPassword.equals(newPassword)) {
                    return Result.error("新密码不能与原密码相同");
                }

                // 3. 将新密码MD5加密后存入数据库
                String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
                dbUser.setPassword(md5NewPassword);
                userService.updateById(dbUser);

                return Result.success("密码修改成功");
            }
}