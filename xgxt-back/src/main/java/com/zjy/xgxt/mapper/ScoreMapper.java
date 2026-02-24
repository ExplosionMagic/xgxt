package com.zjy.xgxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.entity.vo.ScoreVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    @Select("<script>" +
            "SELECT s.id, s.student_no, u.name AS student_name, u.major, " +
            "s.course_id, c.course_name, c.credit, c.teacher_name, s.score, s.status " + // 加上了 s.status
            "FROM score s " +
            "LEFT JOIN user u ON s.student_no = u.user_no " +
            "LEFT JOIN course c ON s.course_id = c.id " +
            "<where>" +
            "  <if test='studentNo != null and studentNo != \"\"'> AND s.student_no = #{studentNo} </if>" +
            "  <if test='courseName != null and courseName != \"\"'> AND c.course_name LIKE CONCAT('%', #{courseName}, '%') </if>" +
            "  <if test='status != null'> AND s.status = #{status} </if>" + // 按状态筛选
            "  <if test='teacherName != null and teacherName != \"\"'> AND c.teacher_name = #{teacherName} </if>" + // 让老师只能看自己的课
            "</where>" +
            "ORDER BY s.id DESC" +
            "</script>")
    List<ScoreVo> getScoreList(@Param("studentNo") String studentNo,
                               @Param("courseName") String courseName,
                               @Param("status") Integer status,
                               @Param("teacherName") String teacherName);
}