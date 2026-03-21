package com.zjy.xgxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("leave_info")
public class LeaveInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String studentNo;   // 学号
    private String studentName; // 学生姓名
    private String reason;      // 请假事由
    private String startTime;   // 开始时间 (可以使用 yyyy-MM-dd HH:mm 格式字符串)
    private String endTime;     // 结束时间

    /**
     * 状态：0-待审批，1-已同意，2-已驳回，3-已销假(可选)
     */
    private Integer status;
    private String approver;    // 审批人姓名 (教职工或管理员)
}