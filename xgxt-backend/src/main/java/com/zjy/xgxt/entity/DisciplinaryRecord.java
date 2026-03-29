package com.zjy.xgxt.entity;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.alibaba.excel.annotation.*;
import lombok.Data;

@Data
@TableName("disciplinary_record")
public class DisciplinaryRecord {
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

    @ExcelProperty("违纪类型")
    private String violationType;

    @ExcelProperty("违纪事实描述")
    @ColumnWidth(40)
    private String description;

    @ExcelProperty("状态 (0待核实 1已处分 2已撤销/驳回)")
    private Integer status;

    @ExcelProperty("处分级别")
    private String punishmentType;

    @ExcelProperty("上报教师")
    private String reporter;

    @ExcelProperty("审批管理员")
    private String adminApprover;

    @ExcelProperty("举报时间")
    private String createTime;
}