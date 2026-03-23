package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Integer id;         // id
    private String title;       // 公告标题
    private String content;     // 公告内容
    private String targetType;  // 可见范围：全校可见, 专业可见, 班级可见
    private String targetValue; // 具体的专业或班级名
    private String publisher;   // 作者
    private String createTime;  // 创建时间
}