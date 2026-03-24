package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("honor_application")
public class HonorApplication {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String studentNo;
    private String studentName;
    private String majorName;
    private String className;
    private String honorName; // 荣誉名称
    private String reason;
    private Integer status;   // 0, 1, 2, 3, 4
    private String teacherApprover;
    private String adminApprover;
    private String applyTime;
}