package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import com.zjy.xgxt.mapper.ScoreMapper;
import com.zjy.xgxt.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score")
@CrossOrigin
public class ScoreController {

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 查询选课/成绩明细列表
     */
    @GetMapping("/list")
    public Result<List<ScoreVo>> list(@RequestParam(required = false) String studentNo,
                                      @RequestParam(required = false) String courseName,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String teacherName) {
        List<ScoreVo> list = scoreMapper.getScoreList(studentNo, courseName, status, teacherName);
        return Result.success(list);
    }

    /**
     * 学生选课 (提交/重新提交 选修申请)
     */
    @PostMapping("/selectCourse")
    public Result<?> selectCourse(@RequestBody Score score) {
        // 1. 查询该学生是否已经选过这门课
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getStudentNo, score.getStudentNo())
                .eq(Score::getCourseId, score.getCourseId());

        Score existScore = scoreService.getOne(wrapper);

        // 2. 如果已经存在记录，根据不同状态给出不同响应
        if (existScore != null) {
            if (existScore.getStatus() == 0) {
                return Result.error("您的申请正在审核中，请耐心等待，勿重复提交");
            } else if (existScore.getStatus() == 1) {
                return Result.error("您已成功选修该课程，无需重复申请");
            } else if (existScore.getStatus() == 2) {
                // 【核心逻辑】：如果是被驳回的记录，将其状态重置为 0 (待审核)
                existScore.setStatus(0);
                scoreService.updateById(existScore);
                return Result.success("已为您重新提交该课程的选修申请，请等待审核！");
            }
        }

        // 3. 如果数据库里没有记录，说明是首次选课，正常新增并设为待审核
        score.setStatus(0);
        scoreService.save(score);
        return Result.success("选修申请已提交，请等待教师审核！");
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
    public Result<?> delete(@PathVariable Integer id) {
        scoreService.removeById(id);
        return Result.success("退课成功");
    }
}