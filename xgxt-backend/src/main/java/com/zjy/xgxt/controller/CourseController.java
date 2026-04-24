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
    @Autowired
    private com.zjy.xgxt.service.ScoreService scoreService;
    @Autowired
    private com.zjy.xgxt.service.UserService userService;

    // 获取课程表
    @GetMapping("/list")
    public Result<List<Course>> list(@RequestParam(required = false) String courseName,
                                     @RequestParam(required = false) String majorName) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(courseName)) {
            wrapper.like(Course::getCourseName, courseName);
        }
        // 按专业筛选课程
        if (StringUtils.hasText(majorName)) {
            wrapper.eq(Course::getMajorName, majorName);
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

    // 查询课程
    @GetMapping("/search")
    public Result<List<Course>> searchCourses(
            @RequestParam(required = false) String nature,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) Integer section,
            @RequestParam(required = false) Boolean hasCapacity) { // 是否有余量

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(nature)) wrapper.eq(Course::getNature, nature);
        if (dayOfWeek != null) wrapper.eq(Course::getDayOfWeek, dayOfWeek);
        if (section != null) wrapper.eq(Course::getSection, section);

        // 如果勾选了“只看有余量”
        if (Boolean.TRUE.equals(hasCapacity)) {
            wrapper.apply("enrolled < capacity");
        }

        wrapper.orderByAsc(Course::getDayOfWeek).orderByAsc(Course::getSection);
        return Result.success(courseService.list(wrapper));
    }

    // 获取课程数据生成个人课表（学生查询选课，老师查询授课）
    @GetMapping("/timetable")
    public Result<List<Course>> getTimetable(@RequestParam String userNo, @RequestParam String role) {
        if ("STUDENT".equals(role)) {
            // 1. 获取该学生所有审核已通过(status=1)的选课记录
            LambdaQueryWrapper<com.zjy.xgxt.entity.Score> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(com.zjy.xgxt.entity.Score::getStudentNo, userNo)
                    .eq(com.zjy.xgxt.entity.Score::getStatus, 1);
            List<com.zjy.xgxt.entity.Score> scores = scoreService.list(scoreWrapper);

            if (scores.isEmpty()) {
                return Result.success(new java.util.ArrayList<>());
            }

            // 2. 提取出所有课程ID，去课程表查询详细信息
            List<Integer> courseIds = scores.stream().map(com.zjy.xgxt.entity.Score::getCourseId).collect(java.util.stream.Collectors.toList());
            return Result.success(courseService.listByIds(courseIds));

        } else if ("TEACHER".equals(role)) {
            // 老师直接查自己名字对应的课程
            com.zjy.xgxt.entity.User teacher = userService.getOne(new LambdaQueryWrapper<com.zjy.xgxt.entity.User>().eq(com.zjy.xgxt.entity.User::getUserNo, userNo));
            if (teacher != null) {
                LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
                courseWrapper.eq(Course::getTeacherName, teacher.getName());
                return Result.success(courseService.list(courseWrapper));
            }
        }
        return Result.success(new java.util.ArrayList<>());
    }
}