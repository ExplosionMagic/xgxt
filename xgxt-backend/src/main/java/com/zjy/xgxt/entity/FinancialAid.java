package com.zjy.xgxt.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("financial_aid")
public class FinancialAid {

    @TableId(type = IdType.AUTO)
    @ExcelIgnore // 忽略该字段，不导出 ID
    private Integer id;

    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("专业")
    @ColumnWidth(20)
    private String majorName;

    @ExcelProperty("班级")
    @ColumnWidth(15)
    private String className;

    @ExcelProperty("申请理由说明")
    @ColumnWidth(40)
    private String reason;

    @ExcelProperty("审批状态 (0待初审 1驳回 2待终审 3终审驳回 4通过)")
    @ColumnWidth(20)
    private Integer status;

    @ExcelProperty("初审教师")
    private String teacherApprover;

    @ExcelProperty("终审管理员")
    private String adminApprover;

    @ExcelProperty("申请时间")
    @ColumnWidth(20)
    private String applyTime;
}