package com.zjy.xgxt.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("class_info")
public class ClassInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String className;
    private String grade;
    private String majorName;
}