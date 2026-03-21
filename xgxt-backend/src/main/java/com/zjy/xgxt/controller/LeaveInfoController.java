package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.LeaveInfo;
import com.zjy.xgxt.service.LeaveInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin
public class LeaveInfoController {

    @Autowired
    private LeaveInfoService leaveInfoService;

    /**
     * 1. 查询请假列表 (支持按学号、状态筛选)
     */
    @GetMapping("/list")
    public Result<List<LeaveInfo>> list(@RequestParam(required = false) String studentNo,
                                        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<LeaveInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(studentNo)) {
            wrapper.eq(LeaveInfo::getStudentNo, studentNo); // 学生只能看自己的
        }
        if (status != null) {
            wrapper.eq(LeaveInfo::getStatus, status); // 教师/管理员可以按状态筛选待审批的
        }
        wrapper.orderByDesc(LeaveInfo::getId);
        return Result.success(leaveInfoService.list(wrapper));
    }

    /**
     * 2. 学生发起请假申请
     */
    @PostMapping("/apply")
    public Result<?> apply(@RequestBody LeaveInfo leaveInfo) {
        leaveInfo.setStatus(0); // 默认待审批
        leaveInfoService.save(leaveInfo);
        return Result.success("请假申请已提交，请等待审批");
    }

    /**
     * 3. 教师/管理员审批请假
     */
    @PutMapping("/audit")
    public Result<?> audit(@RequestBody LeaveInfo leaveInfo) {
        if (leaveInfo.getId() == null || leaveInfo.getStatus() == null) {
            return Result.error("参数异常");
        }
        leaveInfoService.updateById(leaveInfo);
        return Result.success(leaveInfo.getStatus() == 1 ? "已同意该请假申请" : "已驳回该请假申请");
    }

    /**
     * 4. 学生撤销(删除)处于待审批状态的请假
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        leaveInfoService.removeById(id);
        return Result.success("请假申请已撤销");
    }
}