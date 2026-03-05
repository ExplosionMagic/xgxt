package com.zjy.xgxt.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String courseNo; // 课程代码
    private String courseName; // 课程名称
    private BigDecimal credit; // 学分
    private String teacherName; // 授课教师姓名
    private String teacherNo; // 授课教师工号
    private String majorName; // 所属专业
}