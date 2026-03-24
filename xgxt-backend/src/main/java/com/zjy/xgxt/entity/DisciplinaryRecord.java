package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("disciplinary_record")
public class DisciplinaryRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String studentNo;
    private String studentName;
    private String majorName;
    private String className;
    private String violationType;
    private String description;
    private Integer status; // 0-待审批, 1-处分生效, 2-驳回/撤销
    private String reporter;
    private String adminApprover;
    private String createTime;
}