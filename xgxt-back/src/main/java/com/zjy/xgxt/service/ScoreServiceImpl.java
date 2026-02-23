package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import com.zjy.xgxt.mapper.ScoreMapper;
import com.zjy.xgxt.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public List<ScoreVo> getScoreList() {
        return scoreMapper.selectScoreList();
    }

    @Override
    @Transactional // 开启事务
    public void saveScore(Score score) {
        // 1. 检查该学生是否已经修过这门课
        QueryWrapper<Score> query = new QueryWrapper<>();
        query.eq("student_id", score.getStudentId())
                .eq("course_id", score.getCourseId());

        Score existScore = this.getOne(query);

        if (existScore != null) {
            // 2. 如果存在，更新分数
            existScore.setScoreNum(score.getScoreNum());
            this.updateById(existScore);
        } else {
            // 3. 如果不存在，新增记录
            this.save(score);
        }
    }
}
