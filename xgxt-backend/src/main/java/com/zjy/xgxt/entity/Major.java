package com.zjy.xgxt.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Integer id;         // id
    private String majorName;   // 专业名称
//    private String college;     // 所属院部（弃用）
    private String collegeName; // 所属学院
    private String majorCode; // 专业代码
}
