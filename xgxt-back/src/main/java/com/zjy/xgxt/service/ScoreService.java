package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import java.util.List;

public interface ScoreService extends IService<Score> {
    List<ScoreVo> getScoreList(); // 查询VO列表
    void saveScore(Score score);  // 录入成绩逻辑
}
