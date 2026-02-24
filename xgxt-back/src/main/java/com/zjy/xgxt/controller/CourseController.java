package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Course;
import com.zjy.xgxt.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 获取所有课程 (支持按课程名模糊搜索)
    @GetMapping("/list")
    public Result<List<Course>> list(@RequestParam(required = false) String courseName) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(courseName)) {
            wrapper.like(Course::getCourseName, courseName);
        }
        wrapper.orderByDesc(Course::getId);
        return Result.success(courseService.list(wrapper));
    }

    // 新增课程
    @PostMapping
    public Result<?> save(@RequestBody Course course) {
        // 校验课程号是否重复
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseNo, course.getCourseNo());
        if (courseService.count(wrapper) > 0) {
            return Result.error("课程代码已存在");
        }
        courseService.save(course);
        return Result.success("新增课程成功");
    }

    // 修改课程
    @PutMapping
    public Result<?> update(@RequestBody Course course) {
        courseService.updateById(course);
        return Result.success("修改课程成功");
    }

    // 删除课程
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        courseService.removeById(id);
        return Result.success("删除成功");
    }
}