package com.zjy.xgxt.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String majorName;
    private String college;
}
