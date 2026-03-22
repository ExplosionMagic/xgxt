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
    private Integer dayOfWeek; // 星期几
    private Integer section;   // 节次块
    private String location;   // 上课地点
    private String targetGrade; // 推荐年级
    private String nature;      // 必修/选修
    private String remark;      // 备注
    private Integer enrolled;   // 已选人数
    private Integer capacity;   // 课程容量
}