package com.zjy.xgxt.controller;

import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Course;
import com.zjy.xgxt.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseController {
    @Autowired private CourseService courseService;

    @GetMapping("/list")
    public Result<List<Course>> list() {
        return Result.success(courseService.list());
    }

    @PostMapping
    public Result<?> save(@RequestBody Course course) {
        courseService.save(course);
        return Result.success(null);
    }
}
