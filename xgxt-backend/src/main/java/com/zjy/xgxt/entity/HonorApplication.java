package com.zjy.xgxt.entity;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.alibaba.excel.annotation.*;
import lombok.Data;

@Data
@TableName("honor_application")
public class HonorApplication {
    @TableId(type = IdType.AUTO)
    @ExcelIgnore
    private Integer id;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("专业")
    private String majorName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("申请荣誉")
    @ColumnWidth(20)
    private String honorName;

    @ExcelProperty("主要事迹")
    @ColumnWidth(40)
    private String reason;

    @ExcelProperty("状态 (0待初审 1驳回 2待终审 3终审驳回 4通过)")
    private Integer status;

    @ExcelProperty("初审人")
    private String teacherApprover;

    @ExcelProperty("终审人")
    private String adminApprover;

    @ExcelProperty("申请时间")
    private String applyTime;
}