package com.zjy.xgxt.entity.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScoreVo {
    private Integer id;         // 成绩表主键ID
    private String studentNo;   // 学号
    private String studentName; // 学生姓名 (来自user表)
    private String major;       // 学生专业 (来自user表)
    private Integer courseId;   // 课程ID
    private String courseName;  // 课程名称 (来自course表)
    private BigDecimal credit;  // 学分 (来自course表)
    private String teacherName; // 授课教师 (来自course表)
    private BigDecimal score;   // 最终成绩
    private Integer status; // 选修课程状态
}