package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private String targetType;  // ALL, MAJOR, CLASS
    private String targetValue; // 具体的专业或班级名
    private String publisher;
    private String createTime;
}