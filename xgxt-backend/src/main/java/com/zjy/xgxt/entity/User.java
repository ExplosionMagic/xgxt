package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userNo;     // 学号或职工ID
    private String phone;      // 手机号
    private String password;   // 密码
    private String name;       // 姓名
    private String gender;     // 性别
    private String role;       // 用户组 (STUDENT, TEACHER, ADMIN)
    private String major;      // 专业
    private String grade;      // 年级
    private String className;  // 班级
    private String address;    // 联系地址
    private Integer status;    // 状态：1正常，0停用
    private Integer auditStatus; // 学籍审核状态
}