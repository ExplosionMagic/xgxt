package com.zjy.xgxt.entity.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScoreVo {
    private Long id;            // 成绩ID
    private Long studentId;
    private String studentName; // 从学生表查
    private String studentNo;   // 从学生表查
    private Long courseId;
    private String courseName;  // 从课程表查
    private BigDecimal scoreNum;
}
