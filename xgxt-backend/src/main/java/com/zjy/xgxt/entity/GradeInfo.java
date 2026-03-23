package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grade_info")
public class GradeInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;         // id
    private String gradeName;   // 年级名称
    private String createTime;  // 创建时间
}