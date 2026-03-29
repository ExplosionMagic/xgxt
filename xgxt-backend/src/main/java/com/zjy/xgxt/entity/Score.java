package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("score")
public class Score {

    // ==========================================
    // 1. 物理表字段 (score表中真实存在的列)
    // ==========================================
    @TableId(type = IdType.AUTO)
    private Integer id;         // 成绩表主键ID

    private String studentNo;   // 学号

    private Integer courseId;   // 课程ID

    private BigDecimal score;   // 最终成绩

    private Integer status;     // 选修课程状态

    // ==========================================
    // 2. 逻辑/扩展字段 (联表查询带出的列，必须加 exist = false)
    // ==========================================
    @TableField(exist = false)
    private String studentName; // 学生姓名 (来自user表)

    @TableField(exist = false)
    private String major;       // 学生专业 (来自user表)

    @TableField(exist = false)
    private String courseName;  // 课程名称 (来自course表)

    @TableField(exist = false)
    private BigDecimal credit;  // 学分 (来自course表)

    @TableField(exist = false)
    private String teacherName; // 授课教师 (来自course表)
}