package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.mapper.UserMapper;
import com.zjy.xgxt.service.UserService;
import com.zjy.xgxt.utils.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Map<String, Object> login(User userDTO) {
        // 前端登录界面现在会把输入的账号放在 userNo 或者专门的 account 字段传过来
        // 假设前端传过来的账号（可能是学号，也可能是手机号）暂时放在了 userNo 字段里
        String account = userDTO.getUserNo();

        // 1. 根据 学号/职工ID 或者 手机号 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserNo, account).or().eq(User::getPhone, account);
        User dbUser = this.getOne(wrapper);

        // 2. 校验用户是否存在 & 密码是否正确
        if (dbUser == null || !dbUser.getPassword().equals(userDTO.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        // 3. 校验账号状态
        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            throw new RuntimeException("该账号已被管理员停用，禁止登录");
        }

        // 4. 生成 Token
        String token = JwtUtils.generateToken(dbUser.getUserNo());

        // 5. 组装返回数据 (包含最新的实名信息)
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", dbUser.getRole());
        result.put("name", dbUser.getName());     // 返回真实姓名
        result.put("userNo", dbUser.getUserNo()); // 返回学号

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void register(User user) {
        // 1. 必填项校验
        if (!StringUtils.hasText(user.getName()) || !StringUtils.hasText(user.getGender()) ||
                !StringUtils.hasText(user.getPhone()) || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("姓名、性别、手机号、密码为必填项");
        }

        // 2. 检查手机号唯一性
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, user.getPhone());
        if (this.count(phoneWrapper) > 0) {
            throw new RuntimeException("该手机号已被注册，请更换");
        }

        // 3. 确定角色前缀
        // ... (在 register 方法的中间部分)
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("STUDENT"); // 默认学生
        }

        // 学生注册后状态为待完善(0)，教师/管理员直接通过(2)
        if ("STUDENT".equals(user.getRole())) {
            user.setAuditStatus(0);
        } else {
            user.setAuditStatus(2);
        }
        String prefix = "";
        switch (user.getRole()) {
            case "STUDENT": prefix = "S"; break;
            case "TEACHER": prefix = "T"; break;
            case "ADMIN": prefix = "A"; break;
            default: prefix = "U";
        }

        // 4. 生成独立自增学号/职工ID
        // 查询当前角色下，user_no 最大的那条记录
        LambdaQueryWrapper<User> maxIdQuery = new LambdaQueryWrapper<>();
        maxIdQuery.eq(User::getRole, user.getRole())
                .orderByDesc(User::getUserNo) // 按学号倒序排列
                .last("LIMIT 1");             // 只取第一条(最大的)
        User maxUser = this.getOne(maxIdQuery);

        int nextNum = 1; // 默认从 1 开始
        if (maxUser != null && maxUser.getUserNo() != null && maxUser.getUserNo().startsWith(prefix)) {
            // 截取掉前缀，把后面的数字部分转为 int，然后 +1
            String numStr = maxUser.getUserNo().substring(prefix.length());
            try {
                nextNum = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                // 如果解析失败，仍然走默认的 nextNum
            }
        }

        // 拼接成新的学号，例如 S00001, T00002
        String generatedUserNo = prefix + String.format("%05d", nextNum);
        user.setUserNo(generatedUserNo);
        user.setStatus(1); // 默认状态为正常

        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);

        if ("STUDENT".equals(user.getRole())) {
            // 如果管理员在创建时，已经顺手给学生选了专业
            if (StringUtils.hasText(user.getMajor())) {
                // 直接跳过待审核(1)，设为已通过(2)。
                user.setAuditStatus(2);
                // 注意：因为直接通过了，如果系统要求必须有班级，你可能还得要求管理员顺手把班级也选上，或者将其 className 设为某个默认值
            } else {
                // 如果管理员没选专业，那就走原本的流程，让学生自己选
                user.setAuditStatus(1);
            }
        }

        // 5. 直接保存到数据库
        this.save(user);
    }
}