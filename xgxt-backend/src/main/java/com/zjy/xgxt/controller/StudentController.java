package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.ClassInfo;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.ClassInfoService;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClassInfoService classInfoService;

    /**
     * 1. 多条件查询学生列表 (教务管理员用)
     */
    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String majorName,
                                   @RequestParam(required = false) String className) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 核心限制：只查 role 为 STUDENT 的用户，且审核状态为已通过(2)的
        wrapper.eq(User::getRole, "STUDENT").eq(User::getAuditStatus, 2);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getName, keyword).or().like(User::getUserNo, keyword));
        }
        if (StringUtils.hasText(majorName)) {
            wrapper.eq(User::getMajor, majorName);
        }
        if (StringUtils.hasText(className)) {
            wrapper.eq(User::getClassName, className);
        }

        wrapper.orderByDesc(User::getId);
        List<User> list = userService.list(wrapper);
        list.forEach(u -> u.setPassword(null)); // 抹除密码
        return Result.success(list);
    }

    /**
     * 2. 修改学生学籍信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("学生ID不能为空");
        }
        userService.updateById(user);
        return Result.success("学生学籍信息修改成功");
    }

    /**
     * 3. 学生端：自主选择并提交专业
     */
    @PutMapping("/submit-major")
    public Result<?> submitMajor(@RequestBody User user) {
        if (user.getId() == null || !StringUtils.hasText(user.getMajor())) {
            return Result.error("专业选择不能为空");
        }
        User dbUser = userService.getById(user.getId());
        dbUser.setMajor(user.getMajor());
        dbUser.setAuditStatus(1); // 状态变为 1:待审核
        userService.updateById(dbUser);
        return Result.success("专业提交成功，请等待管理员审核");
    }

    /**
     * 4. 管理员端：获取待审核的新生列表
     */
    @GetMapping("/audit-list")
    public Result<List<User>> auditList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT").in(User::getAuditStatus, 1, 3);
        return Result.success(userService.list(wrapper));
    }

    /**
     * 5. 管理员端：审核新生并【系统自动分配班级】
     */
    @PutMapping("/audit")
    public Result<?> auditNewStudent(@RequestBody User user) {
        User dbUser = userService.getById(user.getId());

        if (user.getAuditStatus() == 2) {
            LambdaQueryWrapper<ClassInfo> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.eq(ClassInfo::getMajorName, dbUser.getMajor());
            List<ClassInfo> classes = classInfoService.list(classWrapper);

            if (classes.isEmpty()) {
                return Result.error("分配失败：该专业尚未创建班级，请先去【班级管理】创建！");
            }
            ClassInfo assignedClass = classes.get(0);
            dbUser.setClassName(assignedClass.getClassName());
            dbUser.setGrade(assignedClass.getGrade());
            dbUser.setAuditStatus(2);
            userService.updateById(dbUser);
            return Result.success("审核通过！系统已将其分配至：" + assignedClass.getClassName());

        } else if (user.getAuditStatus() == 3) {
            dbUser.setAuditStatus(3);
            dbUser.setMajor(null);
            userService.updateById(dbUser);
            return Result.success("已驳回，需学生重新选择专业");
        }
        return Result.error("未知操作");
    }
}