package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String studentNo;   // 学号
    private Integer courseId;   // 课程ID
    private BigDecimal score;   // 成绩
    private Integer status;     // 选修课程状态
}