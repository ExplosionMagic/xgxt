package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper; // 【新增】用于更新数据
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.ClassInfo;
import com.zjy.xgxt.entity.User; // 【新增】引入 User 实体
import com.zjy.xgxt.service.ClassInfoService;
import com.zjy.xgxt.service.UserService; // 【新增】引入 UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class_info")
@CrossOrigin // 允许跨域
public class ClassInfoController {

    @Autowired
    private ClassInfoService classInfoService;

    @Autowired
    private UserService userService; // 【新增】注入 UserService 用于同步更新学生数据

    /**
     * 获取所有班级列表 (支持按班级名或专业名模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<ClassInfo>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 输入关键字，既可以搜班级名，也可以搜专业名
            wrapper.like(ClassInfo::getClassName, keyword)
                    .or()
                    .like(ClassInfo::getMajorName, keyword);
        }
        // 按年级和ID排序
        wrapper.orderByDesc(ClassInfo::getGrade).orderByDesc(ClassInfo::getId);
        return Result.success(classInfoService.list(wrapper));
    }

    /**
     * 新增班级
     */
    @PostMapping
    public Result<?> save(@RequestBody ClassInfo classInfo) {
        // 校验班级名是否重复
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassInfo::getClassName, classInfo.getClassName());
        if (classInfoService.count(wrapper) > 0) {
            return Result.error("该班级名称已存在");
        }
        classInfoService.save(classInfo);
        return Result.success("新增班级成功");
    }

    /**
     * 修改班级
     */
    @PutMapping
    public Result<?> update(@RequestBody ClassInfo classInfo) {
        if (classInfo.getId() == null) {
            return Result.error("班级ID不能为空");
        }

        // 1. 先查出数据库里原有的班级信息
        ClassInfo oldClass = classInfoService.getById(classInfo.getId());
        if (oldClass == null) {
            return Result.error("班级不存在");
        }

        // 2. 如果前端传来的新班级名和旧班级名不一样，说明触发了“更名”操作
        if (StringUtils.hasText(classInfo.getClassName()) && !classInfo.getClassName().equals(oldClass.getClassName())) {
            // 去 User 表里把所有属于“旧班级名”的学生，统统改成“新班级名”
            LambdaUpdateWrapper<User> userUpdateWrapper = new LambdaUpdateWrapper<>();
            userUpdateWrapper.eq(User::getClassName, oldClass.getClassName()) // 定位旧名字
                    .set(User::getClassName, classInfo.getClassName()); // 设为新名字
            userService.update(userUpdateWrapper);
        }

        // 3. 最后再更新班级表本身的信息
        classInfoService.updateById(classInfo);
        return Result.success("修改班级成功，相关学生数据已同步");
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        classInfoService.removeById(id);
        return Result.success("删除班级成功");
    }
}