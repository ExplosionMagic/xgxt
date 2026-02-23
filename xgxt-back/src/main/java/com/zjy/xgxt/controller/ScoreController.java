package com.zjy.xgxt.controller;

import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import com.zjy.xgxt.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/score")
@CrossOrigin
public class ScoreController {
    @Autowired private ScoreService scoreService;

    // 获取成绩列表（包含姓名、课程名）
    @GetMapping("/list")
    public Result<List<ScoreVo>> list() {
        return Result.success(scoreService.getScoreList());
    }

    // 录入成绩
    @PostMapping
    public Result<?> save(@RequestBody Score score) {
        scoreService.saveScore(score);
        return Result.success(null);
    }

    // 删除成绩
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        scoreService.removeById(id);
        return Result.success(null);
    }
}
