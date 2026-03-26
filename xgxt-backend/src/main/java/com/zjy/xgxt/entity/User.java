package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.alibaba.excel.annotation.*;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    @ExcelIgnore
    private Integer id;
    @ExcelProperty("学号/教职员工ID")
    private String userNo;     // 学号或职工ID

    @ExcelProperty("手机号（必须唯一）")
    private String phone;      // 手机号

    @ExcelProperty("密码")
    private String password;   // 密码

    @ExcelProperty("姓名")
    private String name;       // 姓名

    @ExcelProperty("性别")
    private String gender;     // 性别

    @ExcelProperty("用户组")
    private String role;       // 用户组 (STUDENT, TEACHER, ADMIN)

    @ExcelProperty("专业")
    private String major;      // 专业

    @ExcelProperty("年级")
    private String grade;      // 年级

    @ExcelProperty("班级")
    private String className;  // 班级

    @ExcelProperty("联系地址")
    private String address;    // 联系地址

    @ExcelProperty("账号状态")
    private Integer status;    // 账号状态：1正常，0停用

    @ExcelProperty("学籍状态")
    private Integer auditStatus; // 学籍审核状态
}