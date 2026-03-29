package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Course;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.mapper.ScoreMapper;
import com.zjy.xgxt.service.CourseService;
import com.zjy.xgxt.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score")
@CrossOrigin
public class ScoreController {

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private CourseService courseService;

    // 查询选课成绩列表
    @GetMapping("/list")
    public Result<List<Score>> list(
            @RequestParam String role,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseName) {

        // 前端会根据角色自动分发参数
        // 直接把参数传给联表查询
        List<Score> list = scoreMapper.getScoreList(studentNo, courseName, teacherName);

        return Result.success(list);
    }

    /**
     * 学生选课 (提交/重新提交 选修申请)
     */
    // 在 ScoreController.java 中重写 selectCourse 方法
    @PostMapping("/selectCourse")
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public Result<?> selectCourse(@RequestBody Score score) {

        // 1. 获取目标课程的详细信息（为了拿到上课时间）
        Course targetCourse = courseService.getById(score.getCourseId());
        if (targetCourse == null) {
            return Result.error("该课程不存在或已被删除");
        }

        // 2. 检查是否已经选过这门课本身
        LambdaQueryWrapper<Score> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Score::getStudentNo, score.getStudentNo())
                .eq(Score::getCourseId, score.getCourseId());
        if (scoreService.count(existWrapper) > 0) {
            return Result.error("您已选修过该课程，无需重复选择");
        }

        // 3. 【新增】上课时间冲突校验
        // 3.1 查出该学生目前已经选上的所有课程的成绩/选课记录
        List<Score> myScores = scoreService.list(
                new LambdaQueryWrapper<Score>().eq(Score::getStudentNo, score.getStudentNo())
        );

        if (!myScores.isEmpty()) {
            // 3.2 提取出所有已选的 courseId
            List<Integer> myCourseIds = myScores.stream()
                    .map(Score::getCourseId)
                    .collect(Collectors.toList());
            // 3.3 去数据库查出这些已选课程的具体时间信息
            List<Course> myCourses = courseService.listByIds(myCourseIds);

            // 3.4 遍历比对时间坐标
            for (Course existingCourse : myCourses) {
                // 如果星期几和节次完全一致，说明在同一个时间格子里面
                if (existingCourse.getDayOfWeek() != null && existingCourse.getSection() != null
                        && existingCourse.getDayOfWeek().equals(targetCourse.getDayOfWeek())
                        && existingCourse.getSection().equals(targetCourse.getSection())) {

                    // 发现冲突，直接终止并返回友好的错误提示
                    return Result.error("选课失败：您在该时段已选修了 "+ existingCourse.getCourseName() +" ");
                }
            }
        }

        // 4. 核心：原子化更新已选人数 (容量控制防超卖)
        boolean updateSuccess = courseService.update(
                new LambdaUpdateWrapper<Course>()
                        .eq(Course::getId, score.getCourseId())
                        .apply("enrolled < capacity") // 确保容量没满
                        .setSql("enrolled = enrolled + 1") // 人数+1
        );

        if (!updateSuccess) {
            return Result.error("该课程无剩余容量！");
        }

        // 5. 生成选课记录，状态直接设为 1 (已通过)
        score.setStatus(1);
        scoreService.save(score);

        return Result.success("选课成功！请在我的课表中查看。");
    }

    /**
     * 教师/管理员 审核选课
     */
    @PutMapping("/audit")
    public Result<?> audit(@RequestBody Score score) {
        if (score.getId() == null || score.getStatus() == null) {
            return Result.error("审核参数异常");
        }
        // 更新状态 (1:通过, 2:驳回)
        scoreService.updateById(score);
        return Result.success(score.getStatus() == 1 ? "已通过该学生的选修申请" : "已驳回选修申请");
    }
    /**
     * 教师/管理员 录入成绩
     */
    @PutMapping("/grade")
    public Result<?> grade(@RequestBody Score score) {
        if (score.getId() == null || score.getScore() == null) {
            return Result.error("成绩数据不完整");
        }
        scoreService.updateById(score);
        return Result.success("成绩录入成功");
    }

    /**
     * 退课 (删除记录)
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class) // 开启事务保证数据一致性
    public Result<?> delete(@PathVariable Integer id) {
        // 1. 先查出这条选课记录，拿到对应的 courseId
        Score score = scoreService.getById(id);
        if (score != null) {
            // 2. 课程表该课程的已选人数 -1 (名额回退)
            courseService.update(
                    new LambdaUpdateWrapper<Course>()
                            .eq(Course::getId, score.getCourseId())
                            .setSql("enrolled = enrolled - 1")
            );
            // 3. 删除学生的选课/成绩记录
            scoreService.removeById(id);
        }
        return Result.success("退课成功，课程名额已释放");
    }
}