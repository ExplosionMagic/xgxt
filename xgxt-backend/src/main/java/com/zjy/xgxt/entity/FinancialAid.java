package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("financial_aid")
public class FinancialAid {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String studentNo;
    private String studentName;
    private String majorName;
    private String className;
    private String reason;
    private Integer status; // 0, 1, 2, 3, 4
    private String teacherApprover;
    private String adminApprover;
    private String applyTime;
}