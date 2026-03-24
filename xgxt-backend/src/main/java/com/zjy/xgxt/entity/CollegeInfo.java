package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("college_info")
public class CollegeInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String collegeName; // 院部名称
    private String dean; // 院长/负责人
    private String createTime;
}