package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.*;
import com.zjy.xgxt.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/major")
@CrossOrigin // 允许跨域
public class MajorController {

    @Autowired
    private MajorService majorService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private CourseService courseService;

    /**
     * 获取所有专业列表 (支持按专业名模糊搜索)
     */
    /**
     * 获取专业列表 (支持按专业名或学院名搜索)
     */
    @GetMapping("/list")
    public Result<List<Major>> list(
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) String collegeName) { // 按学院筛选

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(majorName)) {
            wrapper.like(Major::getMajorName, majorName);
        }
        if (StringUtils.hasText(collegeName)) {
            wrapper.eq(Major::getCollegeName, collegeName); // 学院精准匹配
        }

        wrapper.orderByDesc(Major::getId);
        return Result.success(majorService.list(wrapper));
    }

    /**
     * 新增专业
     */
    @PostMapping
    public Result<?> save(@RequestBody Major major) {
        // 校验专业名是否重复
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorName, major.getMajorName());
        if (majorService.count(wrapper) > 0) {
            return Result.error("该专业名称已存在");
        }
        majorService.save(major);
        return Result.success("新增专业成功");
    }

    /**
     * 修改专业
     */
    @PutMapping
    public Result<?> update(@RequestBody Major major) {
        if (major.getId() == null) {
            return Result.error("专业ID不能为空");
        }

        // 1. 先查出修改前的旧专业信息
        Major oldMajor = majorService.getById(major.getId());
        if (oldMajor == null) {
            return Result.error("该专业不存在");
        }

        String oldName = oldMajor.getMajorName();
        String newName = major.getMajorName();

        // 2. 如果前端传来了新名字，且新名字和旧名字不一样，触发全局同步大招！
        if (StringUtils.hasText(newName) && !newName.equals(oldName)) {

            // 同步 1：更新 User 表（学生和教师所属专业）
            LambdaUpdateWrapper<User> userUpdate = new LambdaUpdateWrapper<>();
            userUpdate.eq(User::getMajor, oldName).set(User::getMajor, newName);
            userService.update(userUpdate);

            // 同步 2：更新 ClassInfo 班级表（班级所属专业）
            LambdaUpdateWrapper<ClassInfo> classUpdate = new LambdaUpdateWrapper<>();
            classUpdate.eq(ClassInfo::getMajorName, oldName).set(ClassInfo::getMajorName, newName);
            classInfoService.update(classUpdate);

            // 同步 3：更新 Course 课程表（课程所属专业）
            LambdaUpdateWrapper<Course> courseUpdate = new LambdaUpdateWrapper<>();
            courseUpdate.eq(Course::getMajorName, oldName).set(Course::getMajorName, newName);
            courseService.update(courseUpdate);

            // 同步 4：更新 Score 成绩表（如果你的成绩表里独立存了 majorName 字段的话）
            /* LambdaUpdateWrapper<Score> scoreUpdate = new LambdaUpdateWrapper<>();
            scoreUpdate.eq(Score::getMajorName, oldName).set(Score::getMajorName, newName);
            scoreService.update(scoreUpdate);
            */
        }

        // 3. 最后更新专业表自身
        majorService.updateById(major);
        return Result.success("专业名称修改成功，系统已自动同步所有关联数据！");
    }

    /**
     * 删除专业
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        majorService.removeById(id);
        return Result.success("删除专业成功");
    }
}
