package com.zjy.xgxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 多表联查：查询成绩列表，包含学生姓名和课程名称
     */
    @Select("SELECT s.id, s.score_num, " +
            "stu.id as student_id, stu.name as student_name, stu.student_no, " +
            "c.id as course_id, c.name as course_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id " +
            "LEFT JOIN course c ON s.course_id = c.id " +
            "ORDER BY s.id DESC")
    List<ScoreVo> selectScoreList();
}
